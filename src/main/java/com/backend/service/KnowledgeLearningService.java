package com.backend.service;

import com.backend.dto.knowledge.KnowledgeBaseResponse;

public interface KnowledgeLearningService {

    KnowledgeBaseResponse learn(String keyword, String category, String userQuestion, String conversationContext);
}