package com.backend.service.impl;

import com.backend.dto.knowledge.KnowledgeBaseRequest;
import com.backend.entity.KnowledgeBase;
import com.backend.repository.KnowledgeBaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseServiceImplTest {

    @Mock
    private KnowledgeBaseRepository repository;

    @InjectMocks
    private KnowledgeBaseServiceImpl service;

    @Test
    void save_shouldNormalizeAndPersistKnowledge() {
        KnowledgeBaseRequest request = new KnowledgeBaseRequest(
                "  Black Myth Wukong  ",
                "  game  ",
                "  Minimum Requirements  ",
                "  Ryzen 5 + RTX 2060  ",
                "  https://example.com/game  "
        );

        when(repository.save(any(KnowledgeBase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.save(request);

        ArgumentCaptor<KnowledgeBase> captor = ArgumentCaptor.forClass(KnowledgeBase.class);
        verify(repository).save(captor.capture());

        KnowledgeBase saved = captor.getValue();
        assertThat(saved.getKeyword()).isEqualTo("black myth wukong");
        assertThat(saved.getCategory()).isEqualTo("game");
        assertThat(saved.getTitle()).isEqualTo("Minimum Requirements");
        assertThat(saved.getSearchCount()).isEqualTo(0);
        assertThat(response.keyword()).isEqualTo("black myth wukong");
    }

    @Test
    void incrementAccess_shouldIncreaseSearchCount() {
        KnowledgeBase entity = new KnowledgeBase();
        entity.setId(1L);
        entity.setKeyword("cyberpunk 2077");
        entity.setCategory("game");
        entity.setTitle("Requirements");
        entity.setContent("content");
        entity.setSearchCount(2);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(KnowledgeBase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.incrementAccess(1L);

        assertThat(response.searchCount()).isEqualTo(3);
        verify(repository).save(any(KnowledgeBase.class));
    }
}