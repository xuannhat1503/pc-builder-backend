package com.backend.dto.cpu;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CpuRequest(
        @NotNull Long baseComponentId,
        @NotBlank @Size(max = 100) String socketType,
        @NotNull @Min(1) Integer tdpWattage
) {
}