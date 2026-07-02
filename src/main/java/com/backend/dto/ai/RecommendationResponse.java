package com.backend.dto.ai;

import java.math.BigDecimal;
import java.util.List;

public record RecommendationResponse(
        String cpu,
        String gpu,
        String ram,
        String mainboard,
        String psu,
        BigDecimal estimatedPrice,
        String reason,
        List<String> cheaperAlternatives,
        List<String> betterUpgrades
) {
}