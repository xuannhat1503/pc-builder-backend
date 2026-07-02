package com.backend.service.impl;

import com.backend.dto.knowledge.KnowledgeBaseRequest;
import com.backend.dto.knowledge.KnowledgeBaseResponse;
import com.backend.entity.KnowledgeBase;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.KnowledgeBaseRepository;
import com.backend.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseRepository repository;

    @Override
    public Optional<KnowledgeBaseResponse> findByKeyword(String keyword) {
        String normalizedKeyword = normalize(keyword);
        if (normalizedKeyword.isBlank()) {
            return Optional.empty();
        }
        return repository.findFirstByKeywordIgnoreCaseOrderByUpdatedAtDesc(normalizedKeyword).map(this::toResponse);
    }

    @Override
    public KnowledgeBaseResponse save(KnowledgeBaseRequest request) {
        KnowledgeBase entity = new KnowledgeBase();
        applyRequest(entity, request);
        entity.setId(null);

        try {
            log.info("[Knowledge] Saving...");
            KnowledgeBase saved = repository.save(entity);

                KnowledgeBase verified = repository
                    .findFirstByKeywordIgnoreCaseOrderByUpdatedAtDesc(saved.getKeyword())
                    .orElseThrow(() -> new IllegalStateException("[Knowledge] Save verification failed for keyword=" + saved.getKeyword()));

            if (verified.getId() == null || !verified.getId().equals(saved.getId())) {
                throw new IllegalStateException("[Knowledge] Save verification returned a different row for keyword=" + saved.getKeyword());
            }

            log.info("[Knowledge] Saved successfully");
            return toResponse(verified);
        } catch (Exception exception) {
            log.error("[Knowledge] Save failed", exception);
            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("[Knowledge] Save failed", exception);
        }
    }

    @Override
    public KnowledgeBaseResponse update(Long id, KnowledgeBaseRequest request) {
        KnowledgeBase entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay knowledge co id = " + id));
        applyRequest(entity, request);
        return toResponse(repository.save(entity));
    }

    @Override
    public KnowledgeBaseResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay knowledge co id = " + id));
    }

    @Override
    public List<KnowledgeBaseResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public Optional<KnowledgeBaseResponse> findMostRelevant(String keyword, String category) {
        log.info("[Knowledge] Searching...");
        String normalizedKeyword = normalize(keyword);
        String normalizedCategory = normalize(category);

        if (!normalizedKeyword.isBlank() && !normalizedCategory.isBlank()) {
            return repository.findFirstByKeywordIgnoreCaseAndCategoryIgnoreCaseOrderBySearchCountDescUpdatedAtDesc(
                    normalizedKeyword, normalizedCategory
            ).map(this::toResponse);
        }

        if (!normalizedKeyword.isBlank()) {
            return repository.findTop10ByKeywordContainingIgnoreCaseOrderBySearchCountDescUpdatedAtDesc(normalizedKeyword)
                    .stream()
                    .findFirst()
                    .map(this::toResponse);
        }

        if (!normalizedCategory.isBlank()) {
            return repository.findTop10ByCategoryIgnoreCaseOrderBySearchCountDescUpdatedAtDesc(normalizedCategory)
                    .stream()
                    .findFirst()
                    .map(this::toResponse);
        }

        return Optional.empty();
    }

    @Override
    public KnowledgeBaseResponse incrementAccess(Long id) {
        KnowledgeBase entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay knowledge co id = " + id));
        entity.setSearchCount(Optional.ofNullable(entity.getSearchCount()).orElse(0) + 1);
        entity.setLastAccess(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        KnowledgeBase entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay knowledge co id = " + id));
        repository.delete(entity);
    }

    private void applyRequest(KnowledgeBase entity, KnowledgeBaseRequest request) {
        entity.setKeyword(normalize(request.keyword()));
        entity.setCategory(normalize(request.category()));
        entity.setTitle(trimToNull(request.title()));
        entity.setContent(trimToNull(request.content()));
        entity.setSourceUrl(trimToNull(request.sourceUrl()));
    }

    private KnowledgeBaseResponse toResponse(KnowledgeBase entity) {
        return new KnowledgeBaseResponse(
                entity.getId(),
                entity.getKeyword(),
                entity.getCategory(),
                entity.getTitle(),
                entity.getContent(),
                entity.getSourceUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getSearchCount(),
                entity.getLastAccess()
        );
    }

    private String normalize(String value) {
        return trimToNull(value) == null ? "" : value.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}