package com.backend.service.impl;

import com.backend.dto.knowledge.KnowledgeBaseRequest;
import com.backend.dto.knowledge.KnowledgeBaseResponse;
import com.backend.dto.tavily.SearchRequest;
import com.backend.dto.tavily.SearchResponse;
import com.backend.dto.tavily.SearchResult;
import com.backend.service.KnowledgeBaseService;
import com.backend.service.KnowledgeSummaryService;
import com.backend.service.TavilySearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeLearningServiceImplTest {

    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    @Mock
    private TavilySearchService tavilySearchService;

    @Mock
    private KnowledgeSummaryService knowledgeSummaryService;

    @InjectMocks
    private KnowledgeLearningServiceImpl service;

    @Test
    void learn_shouldReturnCachedKnowledgeWithoutInternetSearch() {
        KnowledgeBaseResponse cached = new KnowledgeBaseResponse(
                11L,
                "black myth wukong",
                "game_requirement",
                "Game summary",
                "Cached content",
                "https://example.com/cached",
                null,
                null,
                5,
                null
        );
        KnowledgeBaseResponse incremented = new KnowledgeBaseResponse(
                11L,
                "black myth wukong",
                "game_requirement",
                "Game summary",
                "Cached content",
                "https://example.com/cached",
                null,
                null,
                6,
                null
        );

        when(knowledgeBaseService.findByKeyword("black myth wukong")).thenReturn(Optional.of(cached));
        when(knowledgeBaseService.incrementAccess(11L)).thenReturn(incremented);

        KnowledgeBaseResponse response = service.learn(
                "Black Myth Wukong",
                "game_requirement",
                "Build PC for Black Myth Wukong",
                ""
        );

        assertThat(response.searchCount()).isEqualTo(6);
        verify(knowledgeBaseService).incrementAccess(11L);
        verifyNoInteractions(tavilySearchService, knowledgeSummaryService);
    }

    @Test
    void learn_shouldSearchSummarizeSaveAndVerifyKnowledge() {
        SearchResponse searchResponse = new SearchResponse(
                "black myth wukong",
                "answer",
                List.of(
                        new SearchResult(
                                "Steam Requirements",
                                "https://store.steampowered.com/app/2358720",
                                "Minimum CPU ...",
                                0.98,
                                "Minimum CPU ..."
                        )
                ),
                0.42
        );
        KnowledgeBaseResponse saved = new KnowledgeBaseResponse(
                21L,
                "black myth wukong",
                "game_requirement",
                "Knowledge summary for black myth wukong",
                "Summary text from Claude",
                "https://store.steampowered.com/app/2358720",
                null,
                null,
                0,
                null
        );

        when(knowledgeBaseService.findByKeyword("black myth wukong"))
                .thenReturn(Optional.empty(), Optional.of(saved));
        when(knowledgeBaseService.findMostRelevant("black myth wukong", "game_requirement"))
                .thenReturn(Optional.empty());
        when(tavilySearchService.search(any(SearchRequest.class))).thenReturn(searchResponse);
        when(knowledgeSummaryService.summarize(anyString(), anyString(), anyString(), any(SearchResponse.class)))
                .thenReturn("Summary text from Claude");
        when(knowledgeBaseService.save(any(KnowledgeBaseRequest.class))).thenReturn(saved);

        KnowledgeBaseResponse response = service.learn(
                "Black Myth Wukong",
                "game_requirement",
                "Build PC for Black Myth Wukong",
                "previous context"
        );

        assertThat(response.id()).isEqualTo(21L);
        assertThat(response.content()).isEqualTo("Summary text from Claude");

        ArgumentCaptor<SearchRequest> searchCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(tavilySearchService).search(searchCaptor.capture());
        assertThat(searchCaptor.getValue().query()).isEqualTo("black myth wukong");
        assertThat(searchCaptor.getValue().searchDepth()).isEqualTo("advanced");
        assertThat(searchCaptor.getValue().maxResults()).isEqualTo(5);

        ArgumentCaptor<KnowledgeBaseRequest> saveCaptor = ArgumentCaptor.forClass(KnowledgeBaseRequest.class);
        verify(knowledgeBaseService).save(saveCaptor.capture());
        KnowledgeBaseRequest savedRequest = saveCaptor.getValue();
        assertThat(savedRequest.keyword()).isEqualTo("black myth wukong");
        assertThat(savedRequest.category()).isEqualTo("game_requirement");
        assertThat(savedRequest.title()).isEqualTo("Knowledge summary for black myth wukong");
        assertThat(savedRequest.content()).isEqualTo("Summary text from Claude");
        assertThat(savedRequest.sourceUrl()).isEqualTo("https://store.steampowered.com/app/2358720");
    }
}