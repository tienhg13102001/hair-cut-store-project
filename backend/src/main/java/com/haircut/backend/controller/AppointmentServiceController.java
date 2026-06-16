package com.haircut.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haircut.backend.dto.AddServiceToAppointmentRequest;
import com.haircut.backend.dto.ErrorResponse;
import com.haircut.backend.entity.Appointment;
import com.haircut.backend.entity.AppointmentService;
import com.haircut.backend.entity.AppointmentStatus;
import com.haircut.backend.entity.Service;
import com.haircut.backend.entity.ServiceTierPrice;
import com.haircut.backend.entity.Tier;
import com.haircut.backend.repository.AppointmentRepository;
import com.haircut.backend.repository.AppointmentServiceRepository;
import com.haircut.backend.repository.ServiceRepository;
import com.haircut.backend.repository.ServiceTierPriceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointments/{appointmentId}/services")
public class AppointmentServiceController {
  private final AppointmentRepository appointmentRepo;
  private final ServiceRepository serviceRepo;
  private final ServiceTierPriceRepository serviceTierPriceRepo;
  private final AppointmentServiceRepository appointmentServiceRepo;
  // Các trạng thái "terminal" — không cho phép sửa nữa.
  // Dùng cho PUT để chặn update lịch đã đóng (giữ lịch sử bất biến cho
  // audit/invoice).
  private static final List<AppointmentStatus> TERMINAL_STATUSES = List.of(
      AppointmentStatus.COMPLETED,
      AppointmentStatus.CANCELLED,
      AppointmentStatus.NO_SHOW);

  public AppointmentServiceController(AppointmentRepository appointmentRepo, ServiceRepository serviceRepo,
      ServiceTierPriceRepository serviceTierPriceRepo, AppointmentServiceRepository appointmentServiceRepo) {
    this.appointmentRepo = appointmentRepo;
    this.serviceRepo = serviceRepo;
    this.serviceTierPriceRepo = serviceTierPriceRepo;
    this.appointmentServiceRepo = appointmentServiceRepo;
  }

  @PostMapping
  public ResponseEntity<?> addService(
      @PathVariable Long appointmentId,
      @Valid @RequestBody AddServiceToAppointmentRequest req) {
    // ────────────────────────────────────────────────────────────────────────
    // [A] LOAD APPOINTMENT
    // ────────────────────────────────────────────────────────────────────────
    // Bắt buộc — không có entity cũ thì không có gì để merge/validate.
    Optional<Appointment> appointmentOpt = appointmentRepo.findById(appointmentId);
    if (appointmentOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
          "APPOINTMENT_NOT_FOUND",
          "Appointment id=" + appointmentId + " không tồn tại"));
    }
    Appointment appointment = appointmentOpt.get();

    // ────────────────────────────────────────────────────────────────────────
    // [B] TERMINAL CHECK
    // ────────────────────────────────────────────────────────────────────────
    // Lịch ở trạng thái COMPLETED / CANCELLED / NO_SHOW là "đã đóng" —
    // sửa sẽ phá audit trail và có thể conflict với invoice đã in.
    // Phải reject SỚM trước khi load thêm gì.
    if (TERMINAL_STATUSES.contains(appointment.getStatus())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(
          "APPOINTMENT_FINALIZED",
          "Không thể sửa lịch đã ở trạng thái terminal: " + appointment.getStatus()));
    }

    // ────────────────────────────────────────────────────────────────────────
    // [C] Load service by req.serviceId()
    // ────────────────────────────────────────────────────────────────────────
    // → 404 SERVICE_NOT_FOUND nếu rỗng
    Optional<Service> serviceOpt = serviceRepo.findById(req.serviceId());
    if (serviceOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ErrorResponse.of("SERVICE_NOT_FOUND", "Service id=" + req.serviceId() + " không tồn tại"));
    }
    Service service = serviceOpt.get();

    // ────────────────────────────────────────────────────────────────────────
    // [D] Service active check
    // ────────────────────────────────────────────────────────────────────────
    // → 400 SERVICE_INACTIVE nếu !service.isActive()
    if (!service.isActive()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ErrorResponse.of("SERVICE_INACTIVE", "Service id=" + service.getId() + " đã ngưng hoạt động"));
    }

    // ────────────────────────────────────────────────────────────────────────
    // [E] Duplicate check
    // ────────────────────────────────────────────────────────────────────────
    // → 409 SERVICE_ALREADY_ADDED nếu
    // existsByAppointmentIdAndServiceId(appointmentId, req.serviceId())
    boolean isDuplicated = appointmentServiceRepo.existsByAppointmentIdAndServiceId(appointmentId, req.serviceId());
    if (isDuplicated) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(ErrorResponse.of("SERVICE_ALREADY_ADDED",
              "Service id=" + req.serviceId() + " đã được thêm vào appointment id=" + appointmentId));
    }

    // ────────────────────────────────────────────────────────────────────────
    // [F] Lookup tier price
    // ────────────────────────────────────────────────────────────────────────
    // → barber.getTier() → tier
    // → tierPriceRepo.findByServiceIdAndTier(req.serviceId(), tier)
    // → 422 PRICE_NOT_CONFIGURED nếu empty
    // message: "Chưa cấu hình giá cho service id=X, tier=Y"
    Tier tier = appointment.getBarber().getTier();
    Optional<ServiceTierPrice> serviceTierPriceOpt = serviceTierPriceRepo.findByServiceIdAndTier(req.serviceId(), tier);
    if (serviceTierPriceOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
          .body(ErrorResponse.of("PRICE_NOT_CONFIGURED",
              "Chưa cấu hình giá cho service id=" + req.serviceId() + ", tier=" + tier));
    }
    ServiceTierPrice serviceTierPrice = serviceTierPriceOpt.get();

    // ────────────────────────────────────────────────────────────────────────
    // Snapshot + save
    // ────────────────────────────────────────────────────────────────────────
    AppointmentService as = new AppointmentService();
    as.setAppointment(appointment);
    as.setService(service);
    as.setPriceVndAtBooking(serviceTierPrice.getPrice());
    as.setTierAtBooking(tier);

    AppointmentService savedData = appointmentServiceRepo.save(as);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
  }

  @GetMapping
  public ResponseEntity<List<AppointmentService>> listServices(@PathVariable Long appointmentId) {
    // KHÔNG cần check appointment tồn tại — query findByAppointmentId rỗng list nếu
    // appointment không có.
    // Trả 200 + [] (empty list) là chuẩn REST cho "tài nguyên tồn tại nhưng chưa có
    // item con"
    //
    // Hoặc strict hơn: load appointment trước, 404 nếu rỗng → tùy bạn chọn.
    // → Tôi recommend: trả 200 + [] luôn cho đơn giản (đỡ 1 query).
    List<AppointmentService> items = appointmentServiceRepo.findByAppointmentId(appointmentId);

    return ResponseEntity.ok(items);
  }

  @DeleteMapping("/{appointmentServiceId}")
  public ResponseEntity<?> removeService(
      @PathVariable Long appointmentId,
      @PathVariable Long appointmentServiceId) {

    // [A] Load AppointmentService by appointmentServiceId
    // → 404 APPOINTMENT_SERVICE_NOT_FOUND nếu rỗng
    Optional<AppointmentService> appointmentServiceOpt = appointmentServiceRepo.findById(appointmentServiceId);
    if (appointmentServiceOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ErrorResponse.of("APPOINTMENT_SERVICE_NOT_FOUND",
              "AppointmentService id=" + appointmentServiceId + " không tồn tại"));
    }
    AppointmentService as = appointmentServiceOpt.get();
    // [B] Verify nó THUỘC appointmentId (chống URL tampering)
    // Nếu as.getAppointment().getId() != appointmentId → 404 (giả vờ không tồn tại
    // để bảo mật)
    // Lý do: user có thể đoán ID của AppointmentService thuộc lịch khác → cố DELETE
    // nhầm
    // URL: DELETE /appointments/5/services/99 — service 99 có thể thuộc appointment
    // 7
    // → Đáp trả 404 thay vì 403 để không leak thông tin "row 99 tồn tại nhưng không
    // thuộc bạn"
    if (!appointmentId.equals(as.getAppointment().getId())) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ErrorResponse.of("APPOINTMENT_SERVICE_NOT_FOUND",
              "AppointmentService id=" + appointmentServiceId + " không tồn tại"));
    }

    // [C] Terminal check trên appointment cha
    // Nếu as.getAppointment().getStatus() ∈ TERMINAL_STATUSES → 409
    // APPOINTMENT_FINALIZED
    // Reuse constant và message từ POST
    AppointmentStatus appointmentStatus = as.getAppointment().getStatus();
    if (TERMINAL_STATUSES.contains(appointmentStatus)) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(ErrorResponse.of("APPOINTMENT_FINALIZED",
              "Không thể xóa service khỏi lịch đã ở trạng thái terminal: " + appointmentStatus));
    }

    // [D] Delete
    // appointmentServiceRepo.delete(as);
    appointmentServiceRepo.delete(as);

    return ResponseEntity.noContent().build(); // 204 No Content
  }
}
