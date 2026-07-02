package com.backend.dto.compatibility;

import java.math.BigDecimal;
import java.util.List;

public record CompatibilityReport(
        Long buildId,
        Boolean compatible,
        String cpuSocketType,
        String mainboardSocketType,
        String ramGeneration,
        Integer estimatedPowerWatt,
        Integer recommendedPsuWatt,
        BigDecimal totalPrice,
        List<CompatibilityIssue> issues,
        List<String> suggestions
) {
}