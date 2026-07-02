package com.backend.dto.compatibility;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompatibilityCheckComponent(
        @NotNull Long componentId,
        @NotNull @Min(1) Integer quantity
) {
}
