package com.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tavily")
public record TavilyProperties(
        String apiKey,
        String baseUrl,
        String searchDepth,
        String topic,
        Integer maxResults,
        Boolean includeAnswer,
        Boolean includeRawContent,
        Integer connectTimeoutMillis,
        Integer readTimeoutMillis
) {
}