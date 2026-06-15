package com.haircut.backend.dto;

import java.time.OffsetDateTime;

import com.haircut.backend.entity.AppointmentStatus;

import jakarta.validation.constraints.Size;

public record UpdateAppointmentRequest(
    Long branchId, // null = không đổi
    Long barberId,
    @Size(max = 100) String walkInName,
    @Size(max = 20) String walkInPhone,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    AppointmentStatus status, // chuyển trạng thái
    @Size(max = 500) String note) {

}
