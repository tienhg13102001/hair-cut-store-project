package com.haircut.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haircut.backend.dto.CreateAppointmentRequest;
import com.haircut.backend.dto.ErrorResponse;
import com.haircut.backend.entity.Appointment;
import com.haircut.backend.entity.AppointmentStatus;
import com.haircut.backend.entity.BarberProfile;
import com.haircut.backend.entity.Branch;
import com.haircut.backend.entity.User;
import com.haircut.backend.repository.AppointmentRepository;
import com.haircut.backend.repository.BarberProfileRepository;
import com.haircut.backend.repository.BranchRepository;
import com.haircut.backend.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
  private final AppointmentRepository appointmentRepo;
  private final BranchRepository branchRepo;
  private final BarberProfileRepository barberRepo;
  private final UserRepository userRepo;

  public AppointmentController(
      AppointmentRepository appointmentRepo,
      BranchRepository branchRepo,
      BarberProfileRepository barberRepo,
      UserRepository userRepo) {
    this.appointmentRepo = appointmentRepo;
    this.branchRepo = branchRepo;
    this.barberRepo = barberRepo;
    this.userRepo = userRepo;
  }

  @GetMapping()
  public ResponseEntity<List<Appointment>> getAllAppointments() {
    List<Appointment> appointments = appointmentRepo.findAll();
    return ResponseEntity.status(HttpStatus.OK).body(appointments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
    Optional<Appointment> appointment = appointmentRepo.findById(id);
    if (appointment.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.status(HttpStatus.OK).body(appointment.get());
  }

  @GetMapping("/by-customer/{customerId}")
  public ResponseEntity<List<Appointment>> findByCustomer(@PathVariable Long customerId) {
    List<Appointment> appointments = appointmentRepo.findByCustomerId(customerId);
    return ResponseEntity.status(HttpStatus.OK).body(appointments);
  }

  @GetMapping("/by-barber/{barberId}")
  public ResponseEntity<List<Appointment>> findByBarber(@PathVariable Long barberId) {
    List<Appointment> appointments = appointmentRepo.findByBarberId(barberId);
    return ResponseEntity.status(HttpStatus.OK).body(appointments);
  }

  // Return type đổi từ ResponseEntity<Appointment> sang ResponseEntity<?>:
  // - Thành công: body là Appointment
  // - Thất bại: body là ErrorResponse
  // Wildcard <?> cho phép cả 2 kiểu cùng method.
  @PostMapping
  public ResponseEntity<?> createAppointment(@Valid @RequestBody CreateAppointmentRequest body) {
    boolean hasCustomer = body.customerId() != null;
    boolean hasName = StringUtils.hasText(body.walkInName());
    boolean hasPhone = StringUtils.hasText(body.walkInPhone());

    // Validate XOR strict — tách thành 3 nhánh để trả message cụ thể.
    // Thứ tự quan trọng: MIXED trước → NO_CUSTOMER → INCOMPLETE_WALKIN.
    if (hasCustomer && (hasName || hasPhone)) {
      return ResponseEntity.badRequest().body(ErrorResponse.of(
          "MIXED_MODE",
          "Không được vừa gửi customerId vừa gửi walk-in info. Chọn 1 trong 2 mode."));
    }
    if (!hasCustomer && !hasName && !hasPhone) {
      return ResponseEntity.badRequest().body(ErrorResponse.of(
          "NO_CUSTOMER",
          "Phải có customerId (khách đăng ký) HOẶC đủ walkInName+walkInPhone (walk-in)."));
    }
    if (!hasCustomer && (!hasName || !hasPhone)) {
      String missing = !hasName ? "walkInName" : "walkInPhone";
      return ResponseEntity.badRequest().body(ErrorResponse.of(
          "INCOMPLETE_WALKIN",
          "Walk-in cần đủ cả walkInName và walkInPhone. Thiếu: " + missing,
          missing));
    }

    if (!body.endAt().isAfter(body.startAt())) {
      return ResponseEntity.badRequest().body(ErrorResponse.of(
          "INVALID_TIME_RANGE",
          "endAt phải sau startAt. Nhận: startAt=" + body.startAt() + ", endAt=" + body.endAt()));
    }

    Optional<Branch> branch = branchRepo.findById(body.branchId());
    if (branch.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
          "BRANCH_NOT_FOUND",
          "Branch id=" + body.branchId() + " không tồn tại",
          "branchId"));
    }
    Optional<BarberProfile> barberProfile = barberRepo.findById(body.barberId());
    if (barberProfile.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
          "BARBER_NOT_FOUND",
          "BarberProfile id=" + body.barberId() + " không tồn tại",
          "barberId"));
    }
    Long barberBranchId = barberProfile.get().getBranch().getId();
    Long requestBranchId = body.branchId();
    if (!barberBranchId.equals(requestBranchId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ErrorResponse.of("BARBER_NOT_IN_BRANCH",
              "Barber id=" + body.barberId() + " thuộc branch id=" + barberBranchId
                  + ", không khớp branch yêu cầu id=" + requestBranchId,
              "barberId"));
    }
    Optional<User> customer = !hasCustomer
        ? Optional.empty()
        : userRepo.findById(body.customerId());

    if (hasCustomer && customer.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
          "CUSTOMER_NOT_FOUND",
          "User id=" + body.customerId() + " không tồn tại",
          "customerId"));
    }

    List<AppointmentStatus> excluded = List.of(
        AppointmentStatus.CANCELLED,
        AppointmentStatus.NO_SHOW);
    List<Appointment> dataConflict = appointmentRepo.findConflicts(
        barberProfile.get().getId(), body.startAt(), body.endAt(), excluded);
    if (!dataConflict.isEmpty()) {
      // Lấy lịch trùng đầu tiên để báo cho dev biết cụ thể conflict với cái nào.
      Appointment first = dataConflict.get(0);
      return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(
          "TIME_CONFLICT",
          String.format(
              "Thợ id=%d đã có %d lịch trùng. VD: appointmentId=%d (%s → %s, status=%s)",
              body.barberId(),
              dataConflict.size(),
              first.getId(),
              first.getStartAt(),
              first.getEndAt(),
              first.getStatus())));
    }

    Appointment saveAppointment = new Appointment();
    saveAppointment.setBarber(barberProfile.get());
    saveAppointment.setBranch(branch.get());
    saveAppointment.setCustomer(customer.orElse(null));
    saveAppointment.setWalkInName(body.walkInName());
    saveAppointment.setWalkInPhone(body.walkInPhone());
    saveAppointment.setStartAt(body.startAt());
    saveAppointment.setEndAt(body.endAt());
    saveAppointment.setStatus(AppointmentStatus.PENDING);
    saveAppointment.setNote(body.note());

    Appointment saved = appointmentRepo.save(saveAppointment);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

}
