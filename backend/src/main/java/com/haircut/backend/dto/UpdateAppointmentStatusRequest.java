package com.haircut.backend.dto;

import com.haircut.backend.entity.AppointmentStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull AppointmentStatus status // chuyển trạng thái
) {

}
