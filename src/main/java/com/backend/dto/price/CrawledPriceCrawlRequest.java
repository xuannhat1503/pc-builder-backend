package com.backend.dto.price;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrawledPriceCrawlRequest(
        @NotNull Long componentId,
        @NotBlank String sourceUrl,
        String sourceName
) {
}
