package com.haircut.backend.dto;

/**
 * DTO chung cho tất cả response lỗi của API.
 *
 * - code: mã ngắn dạng UPPER_SNAKE_CASE (vd "BRANCH_NOT_FOUND") — FE/client
 *   có thể switch-case theo mã này để hiển thị UI phù hợp.
 * - message: mô tả tiếng Việt cho dev đọc khi debug qua Swagger/Postman.
 * - field: optional — tên field gây lỗi (nếu là validation lỗi), null nếu
 *   không liên quan đến field cụ thể.
 *
 * Dùng record vì DTO immutable, ngắn gọn, không cần getter/setter.
 */
public record ErrorResponse(
    String code,
    String message,
    String field) {

  // Factory method khi không có field cụ thể (vd not found, conflict)
  public static ErrorResponse of(String code, String message) {
    return new ErrorResponse(code, message, null);
  }

  // Factory method khi lỗi gắn với 1 field (vd validation walk-in)
  public static ErrorResponse of(String code, String message, String field) {
    return new ErrorResponse(code, message, field);
  }
}
