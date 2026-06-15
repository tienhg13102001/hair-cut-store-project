package com.haircut.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateServiceTierPriceRequest(
        @NotNull @DecimalMin("0.00") BigDecimal price) {

}
