package com.backend.service;

import com.backend.dto.ai.AiPromptContext;

public interface PromptBuilderService {

    String buildPrompt(AiPromptContext context);
}