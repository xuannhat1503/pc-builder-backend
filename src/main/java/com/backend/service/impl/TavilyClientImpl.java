package com.backend.service.impl;

import com.backend.config.TavilyProperties;
import com.backend.dto.tavily.SearchRequest;
import com.backend.dto.tavily.SearchResponse;
import com.backend.exception.TavilyClientException;
import com.backend.service.TavilyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TavilyClientImpl implements TavilyClient {

    private final RestTemplate tavilyRestTemplate;
    private final TavilyProperties properties;

    @Override
    public SearchResponse search(SearchRequest request) {
        String apiKey = properties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new TavilyClientException("Tavily apiKey is missing");
        }

        String baseUrl = properties.baseUrl() == null || properties.baseUrl().isBlank()
                ? "https://api.tavily.com/search"
                : properties.baseUrl();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("query", request.query());
        payload.put("search_depth", defaultIfBlank(request.searchDepth(), properties.searchDepth(), "advanced"));
        payload.put("topic", defaultIfBlank(request.topic(), properties.topic(), "general"));
        payload.put("max_results", defaultIfNull(request.maxResults(), properties.maxResults(), 5));
        payload.put("include_answer", defaultIfNull(request.includeAnswer(), properties.includeAnswer(), Boolean.TRUE));
        payload.put("include_raw_content", defaultIfNull(request.includeRawContent(), properties.includeRawContent(), Boolean.TRUE));
        payload.put("include_images", defaultIfNull(request.includeImages(), Boolean.FALSE, Boolean.FALSE));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<SearchResponse> response = tavilyRestTemplate.postForEntity(
                    baseUrl,
                    new HttpEntity<>(payload, headers),
                    SearchResponse.class
            );

            SearchResponse body = response.getBody();
            if (body == null) {
                throw new TavilyClientException("Tavily returned empty body");
            }
            return body;
        } catch (HttpStatusCodeException exception) {
            log.error("[Knowledge] Tavily HTTP error status={} body={}", exception.getStatusCode(), exception.getResponseBodyAsString(), exception);
            throw new TavilyClientException("Tavily request failed with status " + exception.getStatusCode() + ": " + exception.getResponseBodyAsString(), exception);
        } catch (RestClientException exception) {
            log.error("[Knowledge] Tavily request failed", exception);
            throw new TavilyClientException("Tavily request failed", exception);
        }
    }

    private String defaultIfBlank(String value, String fallback, String defaultValue) {
        if (value != null && !value.isBlank()) {
            return value;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return defaultValue;
    }

    private Integer defaultIfNull(Integer value, Integer fallback, Integer defaultValue) {
        if (value != null && value > 0) {
            return value;
        }
        if (fallback != null && fallback > 0) {
            return fallback;
        }
        return defaultValue;
    }

    private Boolean defaultIfNull(Boolean value, Boolean fallback, Boolean defaultValue) {
        if (value != null) {
            return value;
        }
        if (fallback != null) {
            return fallback;
        }
        return defaultValue;
    }
}