package com.backend.dto.price;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CrawledPriceRequest(
        @NotNull Long componentId,
        @NotBlank @Size(max = 255) String sourceName,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal priceValue
) {
}