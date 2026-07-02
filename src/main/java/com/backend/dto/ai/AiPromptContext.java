package com.backend.dto.ai;

public record AiPromptContext(
        AssistantIntent intent,
        String userQuestion,
        String conversationContext,
        String catalogText,
        String priceText,
        String recommendationJson,
        String comparisonJson,
        String compatibilityJson
) {
}