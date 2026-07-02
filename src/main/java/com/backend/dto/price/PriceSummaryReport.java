package com.backend.dto.price;

import java.math.BigDecimal;
import java.util.List;

public record PriceSummaryReport(
        Long componentId,
        Integer sourceCount,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal averagePrice,
        String cheapestSource,
        List<PricePoint> prices
) {
    public record PricePoint(
            Long id,
            String sourceName,
            BigDecimal priceValue
    ) {
    }
}