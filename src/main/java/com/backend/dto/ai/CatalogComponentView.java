package com.backend.dto.ai;

import java.math.BigDecimal;

public record CatalogComponentView(
        Long componentId,
        String type,
        String name,
        String brand,
        String socketType,
        String ramGeneration,
        Integer tdpWattage,
        Integer vramSizeGb,
        Integer capacityGb,
        Integer powerOutputWatt,
        String specificationSummary,
        BigDecimal lowestPrice,
        String cheapestSource
) {
    public String promptLine() {
        return "%s | %s | Brand %s | %s | Price %s | Source %s".formatted(
                type,
                name,
                brand,
                specificationSummary,
                lowestPrice,
                cheapestSource
        );
    }
}