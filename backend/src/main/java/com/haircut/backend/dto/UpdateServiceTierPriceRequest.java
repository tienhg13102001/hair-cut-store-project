package com.haircut.backend.dto;

import java.math.BigDecimal;

public record UpdateServiceTierPriceRequest(
    BigDecimal price) {

}
