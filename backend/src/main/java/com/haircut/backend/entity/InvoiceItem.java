package com.haircut.backend.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Phía "N" của quan hệ 1:N — nhiều InvoiceItem thuộc về 1 Invoice.
  // LAZY: chỉ load Invoice khi thực sự gọi getInvoice(), tránh query thừa.
  // nullable = false: một line item KHÔNG thể tồn tại mà không thuộc hóa đơn nào.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private Invoice invoice;

  // SNAPSHOT tên dịch vụ — copy từ Service.name tại thời điểm tạo hóa đơn.
  // length = 150 khớp với Service.name để không bị cắt chuỗi.
  // updatable = false: JPA sẽ KHÔNG bao giờ UPDATE cột này → khóa cứng snapshot.
  @Column(name = "service_name", nullable = false, updatable = false, length = 150)
  private String serviceName;

  // SNAPSHOT giá — copy từ AppointmentService.priceVndAtBooking (vốn đã là snapshot).
  // Đây là "snapshot-of-snapshot": dù service đổi giá hay bị xóa, hóa đơn vẫn bất biến.
  @Column(name = "price_vnd", nullable = false, updatable = false, precision = 12, scale = 2)
  private BigDecimal priceVnd;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  // No-arg constructor — JPA bắt buộc để hydrate object từ DB.
  public InvoiceItem() {
  }

  // "Creation constructor" — chỉ nhận field nghiệp vụ THẬT.
  // Không nhận id (DB tự sinh) và createdAt (@CreationTimestamp tự fill).
  // Bước 4 sẽ gọi: new InvoiceItem(invoice, as.getService().getName(), as.getPriceVndAtBooking())
  public InvoiceItem(Invoice invoice, String serviceName, BigDecimal priceVnd) {
    this.invoice = invoice;
    this.serviceName = serviceName;
    this.priceVnd = priceVnd;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Invoice getInvoice() {
    return invoice;
  }

  public void setInvoice(Invoice invoice) {
    this.invoice = invoice;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public BigDecimal getPriceVnd() {
    return priceVnd;
  }

  public void setPriceVnd(BigDecimal priceVnd) {
    this.priceVnd = priceVnd;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
