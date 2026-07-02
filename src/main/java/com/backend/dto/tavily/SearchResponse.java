package com.backend.dto.tavily;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResponse(
        String query,
        String answer,
        List<SearchResult> results,
        @JsonProperty("response_time") Double responseTime
) {
    public boolean hasResults() {
        return results != null && !results.isEmpty();
    }
}