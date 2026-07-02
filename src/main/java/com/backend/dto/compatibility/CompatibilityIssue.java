package com.backend.dto.compatibility;

public record CompatibilityIssue(
        String code,
        String severity,
        String message
) {
}