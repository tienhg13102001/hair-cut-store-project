package com.haircut.backend.dto;

import java.math.BigDecimal;

import com.haircut.backend.entity.Tier;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateServiceTierPriceRequest(
        @NotNull Long serviceId,
        @NotNull Tier tier,
        @NotNull @DecimalMin("0.00") BigDecimal price) {
}