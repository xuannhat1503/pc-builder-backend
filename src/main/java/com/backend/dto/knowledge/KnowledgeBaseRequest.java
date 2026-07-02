package com.backend.dto.knowledge;

import jakarta.validation.constraints.NotBlank;

public record KnowledgeBaseRequest(
        @NotBlank String keyword,
        @NotBlank String category,
        @NotBlank String title,
        @NotBlank String content,
        String sourceUrl
) {
}