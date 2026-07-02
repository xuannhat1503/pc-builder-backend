package com.backend.dto.builddetail;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BuildDetailRequest(
        @NotNull Long buildId,
        @NotNull Long componentId,
        @NotNull @Min(1) Integer quantity
) {
}