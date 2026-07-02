package com.backend.dto.gpu;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GpuRequest(
        @NotNull Long baseComponentId,
        @NotNull @Min(1) Integer vramSizeGb,
        @NotNull @Min(1) Integer tdpWattage
) {
}