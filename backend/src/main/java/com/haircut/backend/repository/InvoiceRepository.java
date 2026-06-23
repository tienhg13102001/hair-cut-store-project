package com.haircut.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.haircut.backend.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  // Lấy hóa đơn của 1 lịch hẹn (dùng khi GET /appointments/{id}/invoice)
  Optional<Invoice> findByAppointmentId(Long appointmentId);

  // Chống tạo trùng: ở Bước 4, trước khi auto-tạo Invoice phải check cái này.
  // existsBy rẻ hơn findBy + isPresent vì SQL chỉ SELECT 1 / COUNT, không hydrate entity.
  boolean existsByAppointmentId(Long appointmentId);
}
