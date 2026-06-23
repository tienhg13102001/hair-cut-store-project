package com.haircut.backend.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haircut.backend.dto.CreateAppointmentRequest;
import com.haircut.backend.dto.ErrorResponse;
import com.haircut.backend.dto.UpdateAppointmentRequest;
import com.haircut.backend.dto.UpdateAppointmentStatusRequest;
import com.haircut.backend.entity.Appointment;
import com.haircut.backend.entity.AppointmentService;
import com.haircut.backend.entity.AppointmentStatus;
import com.haircut.backend.entity.BarberProfile;
import com.haircut.backend.entity.Branch;
import com.haircut.backend.entity.Invoice;
import com.haircut.backend.entity.InvoiceItem;
import com.haircut.backend.entity.InvoiceStatus;
import com.haircut.backend.entity.User;
import com.haircut.backend.repository.AppointmentRepository;
import com.haircut.backend.repository.AppointmentServiceRepository;
import com.haircut.backend.repository.BarberProfileRepository;
import com.haircut.backend.repository.BranchRepository;
import com.haircut.backend.repository.InvoiceItemRepository;
import com.haircut.backend.repository.InvoiceRepository;
import com.haircut.backend.repository.UserRepository;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
  private final AppointmentRepository appointmentRepo;
  private final BranchRepository branchRepo;
  private final BarberProfileRepository barberRepo;
  private final UserRepository userRepo;
  // 3 repo cho việc auto-tạo hóa đơn khi lịch COMPLETED (Bước 4)
  private final AppointmentServiceRepository appointmentServiceRepo;
  private final InvoiceRepository invoiceRepo;
  private final InvoiceItemRepository invoiceItemRepo;

  // Các trạng thái KHÔNG chiếm slot lịch (lịch đã huỷ / vắng mặt).
  // Dùng cho conflict check ở cả POST và PUT — promote lên class-level để tránh
  // tạo lại List mỗi lần gọi (cheap nhưng cleaner) và đồng bộ 1 nguồn dữ liệu.
  private static final List<AppointmentStatus> EXCLUDED_FROM_CONFLICT = List.of(
      AppointmentStatus.CANCELLED,
      AppointmentStatus.NO_SHOW);

  // Các trạng thái "terminal" — không cho phép sửa nữa.
  // Dùng cho PUT để chặn update lịch đã đóng (giữ lịch sử bất biến cho
  // audit/invoice).
  private static final List<AppointmentStatus> TERMINAL_STATUSES = List.of(
      AppointmentStatus.COMPLETED,
      AppointmentStatus.CANCELLED,
      AppointmentStatus.NO_SHOW);

  // State machine: với mỗi current status, danh sách status được phép chuyển
  // sang.
  // Terminal status (COMPLETED, CANCELLED, NO_SHOW) → empty set (không đi đâu
  // nữa).
  // getOrDefault dùng Set.of() làm fallback nếu thêm enum value mới mà quên
  // update Map
  // → fail-safe: reject thay vì crash.
  private static final Map<AppointmentStatus, Set<AppointmentStatus>> ALLOWED_TRANSITIONS = Map.of(
      AppointmentStatus.PENDING, Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED),
      AppointmentStatus.CONFIRMED,
      Set.of(AppointmentStatus.IN_PROGRESS, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW),
      AppointmentStatus.IN_PROGRESS, Set.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED),
      AppointmentStatus.COMPLETED, Set.of(),
      AppointmentStatus.CANCELLED, Set.of(),
      AppointmentStatus.NO_SHOW, Set.of());

  public AppointmentController(
      AppointmentRepository appointmentRepo,
      BranchRepository branchRepo,
      BarberProfileRepository barberRepo,
      UserRepository userRepo,
      AppointmentServiceRepository appointmentServiceRepo,
      InvoiceRepository invoiceRepo,
      InvoiceItemRepository invoiceItemRepo) {
    this.appointmentRepo = appointmentRepo;
    this.branchRepo = branchRepo;
    this.barberRepo = barberRepo;
    this.userRepo = userRepo;
    this.appointmentServiceRepo = appointmentServiceRepo;
    this.invoiceRepo = invoiceRepo;
    this.invoiceItemRepo = invoiceItemRepo;
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

    List<Appointment> dataConflict = appointmentRepo.findConflicts(
        barberProfile.get().getId(), body.startAt(), body.endAt(), EXCLUDED_FROM_CONFLICT);
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

  @PutMapping("/{id}")
  public ResponseEntity<?> updateAppointment(
      @PathVariable Long id,
      @Valid @RequestBody UpdateAppointmentRequest req) {

    // ────────────────────────────────────────────────────────────────────────
    // [A] LOAD APPOINTMENT
    // ────────────────────────────────────────────────────────────────────────
    // Bắt buộc — không có entity cũ thì không có gì để merge/validate.
    Optional<Appointment> appointmentOpt = appointmentRepo.findById(id);
    if (appointmentOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
          "APPOINTMENT_NOT_FOUND",
          "Appointment id=" + id + " không tồn tại"));
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
    // [C] COMPUTE NEW VALUES (merge req + entity)
    // ────────────────────────────────────────────────────────────────────────
    // Partial PUT: field nào req gửi null = "không đổi" → dùng giá trị cũ.
    // Phải merge TRƯỚC khi validate, không được validate trực tiếp trên req
    // (vì req.startAt() có thể null → NPE).
    OffsetDateTime newStart = req.startAt() != null ? req.startAt() : appointment.getStartAt();
    OffsetDateTime newEnd = req.endAt() != null ? req.endAt() : appointment.getEndAt();

    // ────────────────────────────────────────────────────────────────────────
    // [D] VALIDATE TIME RANGE (trên giá trị đã merge)
    // ────────────────────────────────────────────────────────────────────────
    // Pure CPU — không cần query — fail-fast trước khi load FK.
    // Test ngược logic: "endAt phải sau startAt" → ĐÚNG = endAt.isAfter(startAt)
    // → SAI (reject) = phủ định.
    if (!newEnd.isAfter(newStart)) {
      return ResponseEntity.badRequest().body(ErrorResponse.of(
          "INVALID_TIME_RANGE",
          "endAt phải sau startAt. Nhận: startAt=" + newStart + ", endAt=" + newEnd));
    }

    // ────────────────────────────────────────────────────────────────────────
    // [E] LOAD FK MỚI (nếu user gửi) — 404 nếu ID sai
    // ────────────────────────────────────────────────────────────────────────
    // Pattern bắt buộc 2 nhánh:
    // - req.xId() == null → "không đổi" → dùng entity cũ
    // - req.xId() != null → load DB → nếu empty → 404 (KHÔNG được silent skip)
    // Lưu kết quả vào biến entity (newBranch/newBarber) để dùng cho [F] và [I].
    Branch newBranch;
    if (req.branchId() != null) {
      Optional<Branch> branchOpt = branchRepo.findById(req.branchId());
      if (branchOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
            "BRANCH_NOT_FOUND",
            "Branch id=" + req.branchId() + " không tồn tại",
            "branchId"));
      }
      newBranch = branchOpt.get();
    } else {
      newBranch = appointment.getBranch();
    }

    BarberProfile newBarber;
    if (req.barberId() != null) {
      Optional<BarberProfile> barberOpt = barberRepo.findById(req.barberId());
      if (barberOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
            "BARBER_NOT_FOUND",
            "BarberProfile id=" + req.barberId() + " không tồn tại",
            "barberId"));
      }
      newBarber = barberOpt.get();
    } else {
      newBarber = appointment.getBarber();
    }

    // ────────────────────────────────────────────────────────────────────────
    // [F] BARBER-IN-BRANCH RE-CHECK
    // ────────────────────────────────────────────────────────────────────────
    // Tránh case: user đổi branch sang Hà Nội nhưng giữ thợ ở Sài Gòn.
    // Dùng equals() chứ KHÔNG dùng == (Long boxed, value >127 sẽ fail random).
    Long newBarberBranchId = newBarber.getBranch().getId();
    Long targetBranchId = newBranch.getId();
    if (!newBarberBranchId.equals(targetBranchId)) {
      return ResponseEntity.badRequest().body(ErrorResponse.of(
          "BARBER_NOT_IN_BRANCH",
          "Barber id=" + newBarber.getId() + " thuộc branch id=" + newBarberBranchId
              + ", không khớp branch yêu cầu id=" + targetBranchId,
          "barberId"));
    }

    // ────────────────────────────────────────────────────────────────────────
    // [G] WALK-IN UPDATE GUARD
    // ────────────────────────────────────────────────────────────────────────
    // XOR rule (đã enforce ở POST): 1 lịch hoặc thuộc customer hoặc walk-in,
    // không bao giờ cả 2. PUT phải giữ invariant này.
    // Nếu lịch đã có customer mà user cố set walk-in info → reject.
    boolean wantsWalkInUpdate = req.walkInName() != null || req.walkInPhone() != null;
    if (wantsWalkInUpdate && appointment.getCustomer() != null) {
      return ResponseEntity.badRequest().body(ErrorResponse.of(
          "INVALID_WALKIN_UPDATE",
          "Không thể đặt walk-in info cho lịch khách đã đăng ký (customerId="
              + appointment.getCustomer().getId() + ")"));
    }

    // ────────────────────────────────────────────────────────────────────────
    // [H] CONFLICT CHECK (đắt nhất — đặt cuối + SKIP optimize)
    // ────────────────────────────────────────────────────────────────────────
    // Nếu user không đổi giờ + không đổi thợ → slot không thay đổi → đã pass
    // ở POST hoặc PUT trước → KHÔNG cần query lại. Tiết kiệm 1 round-trip DB.
    boolean slotChanged = req.startAt() != null
        || req.endAt() != null
        || req.barberId() != null;
    if (slotChanged) {
      // Dùng findConflictsExcluding (loại trừ chính lịch đang update) — nếu
      // không, lịch sẽ "tự conflict với chính nó" khi shrink/đổi giờ nhỏ.
      // Dùng newStart/newEnd/newBarber.getId() (đã merge), KHÔNG dùng raw req.
      List<Appointment> conflicts = appointmentRepo.findConflictsExcluding(
          newBarber.getId(), id, newStart, newEnd, EXCLUDED_FROM_CONFLICT);
      if (!conflicts.isEmpty()) {
        Appointment first = conflicts.get(0);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(
            "TIME_CONFLICT",
            String.format(
                "Thợ id=%d đã có %d lịch trùng. VD: appointmentId=%d (%s → %s, status=%s)",
                newBarber.getId(),
                conflicts.size(),
                first.getId(),
                first.getStartAt(),
                first.getEndAt(),
                first.getStatus())));
      }
    }

    // ────────────────────────────────────────────────────────────────────────
    // [I] APPLY CHANGES (chỉ field non-null)
    // ────────────────────────────────────────────────────────────────────────
    // Quan trọng: phải guard `if (req.xxx() != null)` cho TỪNG field, kể cả
    // FK đã load ở [E]. Lý do: nếu req.branchId() null thì newBranch =
    // appointment.getBranch() (cùng object) → setBranch lại là no-op nhưng
    // KHÔNG sai. Tuy nhiên giữ guard cho consistency + dễ đọc intent.
    if (req.branchId() != null)
      appointment.setBranch(newBranch);
    if (req.barberId() != null)
      appointment.setBarber(newBarber);
    if (req.startAt() != null)
      appointment.setStartAt(req.startAt());
    if (req.endAt() != null)
      appointment.setEndAt(req.endAt());
    if (req.walkInName() != null)
      appointment.setWalkInName(req.walkInName());
    if (req.walkInPhone() != null)
      appointment.setWalkInPhone(req.walkInPhone());
    if (req.note() != null)
      appointment.setNote(req.note());

    // ────────────────────────────────────────────────────────────────────────
    // [J] SAVE
    // ────────────────────────────────────────────────────────────────────────
    // JPA tự detect field đã đổi (dirty checking) và sinh UPDATE SQL minimal.
    // @UpdateTimestamp trên `updatedAt` cũng tự bump.
    Appointment saved = appointmentRepo.save(appointment);
    return ResponseEntity.ok(saved);
  }

  @PatchMapping("/{id}/status")
  @Transactional // gói toàn bộ handler trong 1 transaction: nếu tạo Invoice/Item lỗi
                 // giữa chừng → rollback CẢ status change. Không để hóa đơn nửa vời.
  public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long id,
      @Valid @RequestBody UpdateAppointmentStatusRequest req) {

    // [A] Load — 404 APPOINTMENT_NOT_FOUND nếu rỗng
    Optional<Appointment> appointmentOpt = appointmentRepo.findById(id);
    if (appointmentOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
          "APPOINTMENT_NOT_FOUND",
          "Appointment id=" + id + " không tồn tại"));
    }
    Appointment appointment = appointmentOpt.get();

    // [B] Lấy currentStatus = appointment.getStatus()
    // Nếu currentStatus == req.status() → idempotent, có thể return 200 luôn
    // Hoặc reject là INVALID_STATUS_TRANSITION (tùy bạn chọn — recommend:
    // idempotent OK)
    if (TERMINAL_STATUSES.contains(appointment.getStatus())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(
          "APPOINTMENT_FINALIZED",
          "Không thể sửa lịch đã ở trạng thái terminal: " + appointment.getStatus()));
    }

    // [C] Check allowed transition:
    // Cách 1 (gọn): switch (currentStatus) → trả về Set<AppointmentStatus> allowed
    // Cách 2 (sạch): Map<AppointmentStatus, Set<AppointmentStatus>> TRANSITIONS
    // class-level
    // Nếu req.status() KHÔNG nằm trong allowed → 409 INVALID_STATUS_TRANSITION
    // Message: "Không thể chuyển từ X sang Y"
    AppointmentStatus currentStatus = appointment.getStatus();
    Set<AppointmentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
    if (!allowed.contains(req.status())) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(ErrorResponse.of("CANT_TRANSITION_STATUS",
              "Không thể chuyển từ " + currentStatus + " sang " + req.status()));
    }

    // [D] Apply: appointment.setStatus(req.status());
    appointment.setStatus(req.status());
    Appointment savedData = appointmentRepo.save(appointment);

    // [E] Event-driven creation: chuyển sang COMPLETED → tự sinh hóa đơn.
    // == so sánh enum AN TOÀN (enum là singleton, khác Long boxed ở bug cũ).
    if (req.status() == AppointmentStatus.COMPLETED) {
      createInvoiceForAppointment(appointment);
    }

    return ResponseEntity.status(HttpStatus.OK).body(savedData);
  }

  // Auto-tạo Invoice + InvoiceItem từ các AppointmentService của 1 lịch.
  // Gọi nội bộ khi lịch chuyển COMPLETED. private vì KHÔNG phải endpoint —
  // đây là side-effect của state transition (không có POST /invoices riêng).
  private void createInvoiceForAppointment(Appointment appointment) {
    // [1] Chống tạo trùng: PATCH có thể bị gọi lại, hoặc 1 lịch chỉ 1 hóa đơn
    // (@OneToOne unique). existsBy rẻ hơn findBy + isPresent.
    if (invoiceRepo.existsByAppointmentId(appointment.getId())) {
      return;
    }

    // [2] Load tất cả dịch vụ đã book của lịch (nguồn để copy snapshot).
    // Lịch không có dịch vụ nào → list rỗng → hóa đơn total = 0 (vẫn hợp lệ).
    List<AppointmentService> bookedServices = appointmentServiceRepo.findByAppointmentId(appointment.getId());

    // [3] Tạo Invoice ở trạng thái DRAFT. totalVnd tạm = 0, sẽ cộng dồn ở dưới.
    // Phải save TRƯỚC khi tạo InvoiceItem để Invoice có id (FK invoice_id).
    Invoice invoice = new Invoice();
    invoice.setAppointment(appointment);
    invoice.setStatus(InvoiceStatus.DRAFT);
    invoice.setTotalVnd(BigDecimal.ZERO);
    Invoice savedInvoice = invoiceRepo.save(invoice);

    // [4] Aggregate: vừa tạo từng InvoiceItem (snapshot-of-snapshot),
    // vừa cộng dồn giá vào total.
    BigDecimal total = BigDecimal.ZERO;
    for (AppointmentService booked : bookedServices) {
      InvoiceItem item = new InvoiceItem(
          savedInvoice,
          booked.getService().getName(), // snapshot tên (LAZY load OK nhờ OSIV)
          booked.getPriceVndAtBooking()); // snapshot giá (đã snapshot 1 lần ở booking)
      invoiceItemRepo.save(item);
      total = total.add(booked.getPriceVndAtBooking()); // BigDecimal bất biến → phải gán lại
    }

    // [5] Chốt total cho Invoice rồi save lần cuối.
    savedInvoice.setTotalVnd(total);
    invoiceRepo.save(savedInvoice);
  }

  @GetMapping("/{id}/invoice")
  public ResponseEntity<?> getInvoiceById(@PathVariable Long id) {
    Optional<Invoice> foundInvoiceOpt = invoiceRepo.findByAppointmentId(id);
    if (foundInvoiceOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ErrorResponse.of("INVOICE_NOT_FOUND", "Lịch id=" + id + " chưa có hóa đơn"));
    }
    Invoice foundInvoice = foundInvoiceOpt.get();
    return ResponseEntity.status(HttpStatus.OK).body(foundInvoice);
  }

}
