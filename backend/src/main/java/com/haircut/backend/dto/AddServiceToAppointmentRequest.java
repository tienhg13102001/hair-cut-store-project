package com.haircut.backend.dto;

import jakarta.validation.constraints.NotNull;

public record AddServiceToAppointmentRequest(@NotNull Long serviceId) {

}
