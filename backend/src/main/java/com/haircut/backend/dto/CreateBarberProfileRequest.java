package com.haircut.backend.dto;

import com.haircut.backend.entity.Tier;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBarberProfileRequest(
                @NotNull Long userId,
                @NotNull Long branchId,
                @NotNull Tier tier,
                @Size(max = 1000) String bio,
                @Min(0) @Max(50) Integer yearsExp) {
}
