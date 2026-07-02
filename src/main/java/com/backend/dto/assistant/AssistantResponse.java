package com.backend.dto.assistant;

import com.backend.dto.compatibility.CompatibilityReport;
import com.backend.dto.price.PriceSummaryReport;

import java.util.List;

public record AssistantResponse(
        String answer,
        List<String> recommendations,
        CompatibilityReport compatibility,
        PriceSummaryReport priceSummary
) {
}