package com.backend.dto.tavily;

public record SearchRequest(
        String query,
        String searchDepth,
        String topic,
        Integer maxResults,
        Boolean includeAnswer,
        Boolean includeRawContent,
        Boolean includeImages
) {
}