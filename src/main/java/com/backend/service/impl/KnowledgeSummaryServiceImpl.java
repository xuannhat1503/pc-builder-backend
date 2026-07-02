package com.backend.service.impl;

import com.backend.dto.tavily.SearchResponse;
import com.backend.service.ClaudeClientService;
import com.backend.service.KnowledgeSummaryPromptBuilder;
import com.backend.service.KnowledgeSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeSummaryServiceImpl implements KnowledgeSummaryService {

    private final ClaudeClientService claudeClientService;
    private final KnowledgeSummaryPromptBuilder promptBuilder;

    @Override
    public String summarize(String userQuestion, String keyword, String category, SearchResponse searchResponse) {
        String prompt = promptBuilder.buildPrompt(userQuestion, keyword, category, searchResponse);
        String summary = claudeClientService.ask(prompt);
        if (summary == null || summary.isBlank()) {
            throw new IllegalStateException("[Knowledge] Claude summary is empty");
        }
        return summary.trim();
    }
}