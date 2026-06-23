package com.haircut.backend.dto;

import java.math.BigDecimal;

import com.haircut.backend.entity.InvoiceItem;

// DTO trả ra cho 1 dòng hóa đơn. Record = bất biến, chỉ chứa field cần hiển thị.
// KHÔNG có invoice/appointment lồng nhau → tránh đổ cả cây object + rác lazy proxy.
public record InvoiceItemResponse(
    Long id,
    String serviceName,
    BigDecimal priceVnd) {

  // Static factory: map từ entity → DTO. Đặt ngay trong DTO để gom logic chuyển đổi
  // 1 chỗ (giống ErrorResponse.of). Controller chỉ việc gọi, không tự new.
  public static InvoiceItemResponse from(InvoiceItem item) {
    return new InvoiceItemResponse(
        item.getId(),
        item.getServiceName(),
        item.getPriceVnd());
  }
}
