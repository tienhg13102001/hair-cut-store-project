package com.haircut.backend.entity;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointments", check = @CheckConstraint(name = "ck_appointment_time_range", constraint = "end_at > start_at"))
public class Appointment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", nullable = false)
  private Branch branch;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "barber_id", nullable = false)
  private BarberProfile barber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id") // ← KHÔNG nullable=false (walk-in cho phép null)
  private User customer;

  @Column(name = "walk_in_name", length = 100)
  private String walkInName;

  @Column(name = "walk_in_phone", length = 20)
  private String walkInPhone;

  @Column(name = "start_at", nullable = false)
  private OffsetDateTime startAt;

  @Column(name = "end_at", nullable = false)
  private OffsetDateTime endAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AppointmentStatus status;

  @Column(columnDefinition = "TEXT")
  private String note;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  public Appointment() {
  }

  public Appointment(Long id, Branch branch, BarberProfile barber, User customer, String walkInName, String walkInPhone,
      OffsetDateTime startAt, OffsetDateTime endAt, AppointmentStatus status, String note, OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    this.id = id;
    this.branch = branch;
    this.barber = barber;
    this.customer = customer;
    this.walkInName = walkInName;
    this.walkInPhone = walkInPhone;
    this.startAt = startAt;
    this.endAt = endAt;
    this.status = status;
    this.note = note;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Branch getBranch() {
    return branch;
  }

  public void setBranch(Branch branch) {
    this.branch = branch;
  }

  public BarberProfile getBarber() {
    return barber;
  }

  public void setBarber(BarberProfile barber) {
    this.barber = barber;
  }

  public User getCustomer() {
    return customer;
  }

  public void setCustomer(User customer) {
    this.customer = customer;
  }

  public String getWalkInName() {
    return walkInName;
  }

  public void setWalkInName(String walkInName) {
    this.walkInName = walkInName;
  }

  public String getWalkInPhone() {
    return walkInPhone;
  }

  public void setWalkInPhone(String walkInPhone) {
    this.walkInPhone = walkInPhone;
  }

  public OffsetDateTime getStartAt() {
    return startAt;
  }

  public void setStartAt(OffsetDateTime startAt) {
    this.startAt = startAt;
  }

  public OffsetDateTime getEndAt() {
    return endAt;
  }

  public void setEndAt(OffsetDateTime endAt) {
    this.endAt = endAt;
  }

  public AppointmentStatus getStatus() {
    return status;
  }

  public void setStatus(AppointmentStatus status) {
    this.status = status;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
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
