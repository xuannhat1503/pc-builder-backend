package com.backend.service.impl;

import com.backend.dto.knowledge.KnowledgeBaseRequest;
import com.backend.dto.knowledge.KnowledgeBaseResponse;
import com.backend.dto.tavily.SearchRequest;
import com.backend.dto.tavily.SearchResponse;
import com.backend.exception.TavilyClientException;
import com.backend.service.KnowledgeBaseService;
import com.backend.service.KnowledgeLearningService;
import com.backend.service.KnowledgeSummaryService;
import com.backend.service.TavilySearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeLearningServiceImpl implements KnowledgeLearningService {

    private final KnowledgeBaseService knowledgeBaseService;
    private final TavilySearchService tavilySearchService;
    private final KnowledgeSummaryService knowledgeSummaryService;

    @Override
    public KnowledgeBaseResponse learn(String keyword, String category, String userQuestion, String conversationContext) {
        String normalizedKeyword = normalize(keyword);
        String normalizedCategory = normalize(category);

        log.info("[Knowledge] Searching...");
        Optional<KnowledgeBaseResponse> cached = knowledgeBaseService.findByKeyword(normalizedKeyword);
        if (cached.isEmpty()) {
            cached = knowledgeBaseService.findMostRelevant(normalizedKeyword, normalizedCategory);
        }

        if (cached.isPresent()) {
            return knowledgeBaseService.incrementAccess(cached.get().id());
        }

        SearchRequest request = new SearchRequest(
                buildQuery(normalizedKeyword, userQuestion),
                "advanced",
            "general",
                5,
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.FALSE
        );

        SearchResponse searchResponse;
        try {
            searchResponse = tavilySearchService.search(request);
        } catch (TavilyClientException exception) {
            log.error("[Knowledge] Internet search failed", exception);
            throw exception;
        }

        log.info("[Knowledge] Internet search finished");

        if (searchResponse == null || !searchResponse.hasResults()) {
            throw new IllegalStateException("[Knowledge] No internet results found for keyword=" + normalizedKeyword);
        }

        String summary = knowledgeSummaryService.summarize(userQuestion, normalizedKeyword, normalizedCategory, searchResponse);
        log.info("[Knowledge] Claude summary finished");

        String sourceUrl = extractSourceUrl(searchResponse, normalizedKeyword);
        KnowledgeBaseRequest saveRequest = new KnowledgeBaseRequest(
                normalizedKeyword,
                normalizedCategory,
                buildTitle(normalizedKeyword),
                summary,
                sourceUrl
        );

        KnowledgeBaseResponse saved = knowledgeBaseService.save(saveRequest);
        KnowledgeBaseResponse verified = knowledgeBaseService.findByKeyword(normalizedKeyword)
                .orElseThrow(() -> new IllegalStateException("[Knowledge] Verification failed after save for keyword=" + normalizedKeyword));

        if (verified.id() == null || !verified.id().equals(saved.id())) {
            throw new IllegalStateException("[Knowledge] Save verification mismatch for keyword=" + normalizedKeyword);
        }

        return verified;
    }

    private String buildQuery(String keyword, String userQuestion) {
        String question = userQuestion == null ? "" : userQuestion.trim();
        if (!keyword.isBlank()) {
            return keyword;
        }
        return question;
    }

    private String extractSourceUrl(SearchResponse searchResponse, String keyword) {
        if (searchResponse.results() != null) {
            return searchResponse.results().stream()
                    .map(result -> result.url())
                    .filter(url -> url != null && !url.isBlank())
                    .findFirst()
                    .orElse(buildFallbackSourceUrl(keyword));
        }
        return buildFallbackSourceUrl(keyword);
    }

    private String buildFallbackSourceUrl(String keyword) {
        String encoded = keyword == null ? "" : keyword.trim().replaceAll("\\s+", "%20");
        return "https://api.tavily.com/search?q=" + encoded;
    }

    private String buildTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "Knowledge summary";
        }
        return "Knowledge summary for " + keyword;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}