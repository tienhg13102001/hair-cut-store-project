package com.haircut.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.haircut.backend.entity.AppointmentService;

public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, Long> {
  // Lấy tất cả service đã thêm vào 1 lịch (dùng cho GET endpoint)
  List<AppointmentService> findByAppointmentId(Long appointmentId);

  // Check duplicate trước khi POST (rẻ hơn findBy + isPresent)
  boolean existsByAppointmentIdAndServiceId(Long appointmentId, Long serviceId);

  // (Optional) đếm số service trong 1 lịch. Có thể skip nếu không cần ngay.
  long countByAppointmentId(Long appointmentId);
}
