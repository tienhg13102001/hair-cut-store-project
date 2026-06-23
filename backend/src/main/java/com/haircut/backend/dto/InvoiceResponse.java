package com.haircut.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.haircut.backend.entity.Invoice;
import com.haircut.backend.entity.InvoiceItem;
import com.haircut.backend.entity.InvoiceStatus;

// DTO trả ra cho cả hóa đơn. Chỉ phơi field cần thiết:
// - appointmentId (Long) thay vì cả object Appointment → cắt đứt cây lồng nhau.
// - items: danh sách DTO con (DTO lồng DTO), KHÔNG phải entity.
public record InvoiceResponse(
    Long id,
    Long appointmentId,
    BigDecimal totalVnd,
    InvoiceStatus status,
    OffsetDateTime paidAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<InvoiceItemResponse> items) {

  // Factory nhận invoice + list item (controller load riêng qua repository).
  // Tách 2 tham số vì Invoice entity KHÔNG giữ quan hệ @OneToMany tới item
  // (ta cố ý không map chiều đó để entity gọn) → phải truyền items vào.
  public static InvoiceResponse from(Invoice invoice, List<InvoiceItem> items) {
    // map từng InvoiceItem → InvoiceItemResponse bằng stream.
    List<InvoiceItemResponse> itemResponses = items.stream()
        .map(InvoiceItemResponse::from)
        .toList();

    return new InvoiceResponse(
        invoice.getId(),
        invoice.getAppointment().getId(), // chỉ lấy id — getId() trên lazy proxy KHÔNG query DB
        invoice.getTotalVnd(),
        invoice.getStatus(),
        invoice.getPaidAt(),
        invoice.getCreatedAt(),
        invoice.getUpdatedAt(),
        itemResponses);
  }
}
