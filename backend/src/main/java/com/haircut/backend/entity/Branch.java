package com.haircut.backend.entity;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "branches")
public class Branch {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(nullable = false, length = 300)
  private String address;

  @Column(length = 20)
  private String phone;

  @Column(nullable = false, name = "opening_at")
  private LocalTime openingAt;

  @Column(nullable = false, name = "closing_at")
  private LocalTime closingAt;

  @Column(nullable = false)
  private boolean active = true;

  @CreationTimestamp
  @Column(nullable = false, name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false, name = "updated_at")
  private OffsetDateTime updatedAt;

  // === Constructors ===
  public Branch() {
  } // JPA bắt buộc có constructor rỗng

  public Branch(String name, String address, String phone, LocalTime openingAt, LocalTime closingAt) {
    this.name = name;
    this.address = address;
    this.phone = phone;
    this.openingAt = openingAt;
    this.closingAt = closingAt;
  }

  // === Getter & Setter ===

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public LocalTime getOpeningAt() {
    return openingAt;
  }

  public void setOpeningAt(LocalTime openingAt) {
    this.openingAt = openingAt;
  }

  public LocalTime getClosingAt() {
    return closingAt;
  }

  public void setClosingAt(LocalTime closingAt) {
    this.closingAt = closingAt;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
