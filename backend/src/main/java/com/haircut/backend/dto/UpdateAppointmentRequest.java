package com.haircut.backend.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Size;

public record UpdateAppointmentRequest(
    Long branchId, // null = không đổi
    Long barberId,
    @Size(max = 100) String walkInName,
    @Size(max = 20) String walkInPhone,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    @Size(max = 500) String note) {

}
