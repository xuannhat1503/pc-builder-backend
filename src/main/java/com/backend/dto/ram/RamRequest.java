package com.backend.dto.ram;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RamRequest(
        @NotNull Long baseComponentId,
        @NotBlank @Size(max = 50) String ramGeneration,
        @NotNull @Min(1) Integer capacityGb
) {
}