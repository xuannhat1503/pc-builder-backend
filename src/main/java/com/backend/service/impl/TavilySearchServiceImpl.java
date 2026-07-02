package com.backend.service.impl;

import com.backend.dto.tavily.SearchRequest;
import com.backend.dto.tavily.SearchResponse;
import com.backend.service.TavilyClient;
import com.backend.service.TavilySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TavilySearchServiceImpl implements TavilySearchService {

    private final TavilyClient tavilyClient;

    @Override
    public SearchResponse search(SearchRequest request) {
        return tavilyClient.search(request);
    }
}