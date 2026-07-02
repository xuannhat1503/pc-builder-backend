package com.backend.dto.knowledge;

import java.time.LocalDateTime;

public record KnowledgeBaseResponse(
        Long id,
        String keyword,
        String category,
        String title,
        String content,
        String sourceUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer searchCount,
        LocalDateTime lastAccess
) {
}