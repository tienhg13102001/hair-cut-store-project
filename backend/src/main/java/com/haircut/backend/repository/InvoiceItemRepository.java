package com.haircut.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.haircut.backend.entity.InvoiceItem;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
  // Liệt kê các dòng line-item của 1 hóa đơn (dùng khi hiển thị chi tiết hóa đơn)
  List<InvoiceItem> findByInvoiceId(Long invoiceId);
}
