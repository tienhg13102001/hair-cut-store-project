package com.haircut.backend.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "appointment_services", uniqueConstraints = @UniqueConstraint(name = "uk_appointment_service", columnNames = {
    "appointment_id", "service_id" }))
public class AppointmentService {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appointment_id", nullable = false)
  private Appointment appointment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_id", nullable = false)
  private Service service;

  @Column(name = "price_vnd_at_booking", nullable = false, updatable = false, precision = 12, scale = 2)
  private BigDecimal priceVndAtBooking;

  @Enumerated(EnumType.STRING)
  @Column(name = "tier_at_booking", nullable = false, updatable = false, length = 20)
  private Tier tierAtBooking;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  public AppointmentService() {

  }

  public AppointmentService(Long id, Appointment appointment, Service service, BigDecimal priceVndAtBooking,
      Tier tierAtBooking, OffsetDateTime createdAt) {
    this.id = id;
    this.appointment = appointment;
    this.service = service;
    this.priceVndAtBooking = priceVndAtBooking;
    this.tierAtBooking = tierAtBooking;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Appointment getAppointment() {
    return appointment;
  }

  public void setAppointment(Appointment appointment) {
    this.appointment = appointment;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  public BigDecimal getPriceVndAtBooking() {
    return priceVndAtBooking;
  }

  public void setPriceVndAtBooking(BigDecimal priceVndAtBooking) {
    this.priceVndAtBooking = priceVndAtBooking;
  }

  public Tier getTierAtBooking() {
    return tierAtBooking;
  }

  public void setTierAtBooking(Tier tierAtBooking) {
    this.tierAtBooking = tierAtBooking;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

}
