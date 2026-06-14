package com.haircut.backend.dto;

import com.haircut.backend.entity.Tier;

public record CreateBarberProfileRequest(
        Long userId, // ← flat, không nested
        Long branchId,
        Tier tier,
        String bio,
        Integer yearsExp) {
}
