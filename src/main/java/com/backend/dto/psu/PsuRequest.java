package com.backend.dto.psu;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PsuRequest(
        @NotNull Long baseComponentId,
        @NotNull @Min(1) Integer powerOutputWatt
) {
}