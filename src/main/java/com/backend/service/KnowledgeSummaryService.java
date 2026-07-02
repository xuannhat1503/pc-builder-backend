package com.backend.service;

import com.backend.dto.tavily.SearchResponse;

public interface KnowledgeSummaryService {

    String summarize(String userQuestion, String keyword, String category, SearchResponse searchResponse);
}