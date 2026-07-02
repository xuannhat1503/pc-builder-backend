package com.backend.dto.mainboard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MainboardRequest(
        @NotNull Long baseComponentId,
        @NotBlank @Size(max = 100) String socketType,
        @NotBlank @Size(max = 50) String ramGeneration
) {
}