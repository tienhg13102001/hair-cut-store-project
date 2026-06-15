package com.haircut.backend.dto;

import java.math.BigDecimal;

import com.haircut.backend.entity.Tier;

public record CreateServiceTierPriceRequest(
    Long serviceId,
    Tier tier,
    BigDecimal price) {
}