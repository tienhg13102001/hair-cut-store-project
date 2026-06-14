package com.haircut.backend.dto;

import com.haircut.backend.entity.Tier;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateBarberProfileRequest(
        Long branchId,

        Tier tier,

        @Size(max = 1000) String bio,

        @Min(0) @Max(50) Integer yearsExp,

        @DecimalMin("0.0") @DecimalMax("5.0") // ← chỉ cho rating 0-5
        BigDecimal ratingAvg,

        Boolean active) {
}