package com.backend.dto.build;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BuildRequest(
        @NotNull Long userId,
        String title,
        String description,
        @DecimalMin(value = "0.00", inclusive = true) BigDecimal totalPrice,
        Boolean compatible
) {
}