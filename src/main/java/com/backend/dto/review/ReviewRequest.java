package com.backend.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull Long userId,
        @NotNull Long componentId,
        @NotNull @Min(1) @Max(5) Integer ratingStar,
        String commentText
) {
}