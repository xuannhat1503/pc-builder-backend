package com.backend.dto.tavily;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResult(
        String title,
        String url,
        String content,
        Double score,
        @JsonProperty("raw_content") String rawContent
) {
}