package com.backend.dto.compatibility;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record CompatibilityCheckRequest(
        BigDecimal totalPrice,
        @NotEmpty @Valid List<CompatibilityCheckComponent> components
) {
}
