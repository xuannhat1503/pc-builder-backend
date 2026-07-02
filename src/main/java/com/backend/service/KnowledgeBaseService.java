package com.backend.service;

import com.backend.dto.knowledge.KnowledgeBaseRequest;
import com.backend.dto.knowledge.KnowledgeBaseResponse;

import java.util.List;
import java.util.Optional;

public interface KnowledgeBaseService {

    Optional<KnowledgeBaseResponse> findByKeyword(String keyword);

    KnowledgeBaseResponse save(KnowledgeBaseRequest request);

    KnowledgeBaseResponse update(Long id, KnowledgeBaseRequest request);

    KnowledgeBaseResponse findById(Long id);

    List<KnowledgeBaseResponse> findAll();

    Optional<KnowledgeBaseResponse> findMostRelevant(String keyword, String category);

    KnowledgeBaseResponse incrementAccess(Long id);

    void delete(Long id);
}