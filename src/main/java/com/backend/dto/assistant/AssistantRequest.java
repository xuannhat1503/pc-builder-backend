package com.backend.dto.assistant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record AssistantRequest(
        @NotBlank String message,
        Long componentId,
        BigDecimal budget,
        String useCase,
        @NotEmpty @Valid List<CompatibilityItem> compatibilityComponents,
        String conversationContext
) {
    public record CompatibilityItem(Long componentId, Integer quantity) {
    }
}