package com.haircut.backend.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppointmentRequest(
    @NotNull Long branchId,
    @NotNull Long barberId,
    Long customerId,
    @Size(max = 100) String walkInName,
    @Size(max = 20) String walkInPhone,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @Size(max = 500) String note) {

}
