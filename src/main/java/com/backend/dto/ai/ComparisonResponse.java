package com.backend.dto.ai;

import java.util.List;

public record ComparisonResponse(
        String left,
        String right,
        String verdict,
        String reason,
        List<String> differences
) {
}