package com.backend.service;

import com.backend.dto.tavily.SearchRequest;
import com.backend.dto.tavily.SearchResponse;

public interface TavilySearchService {

    SearchResponse search(SearchRequest request);
}