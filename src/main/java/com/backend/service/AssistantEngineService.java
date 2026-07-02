package com.backend.service;

import com.backend.dto.ai.ChatResponse;
import com.backend.dto.assistant.AssistantRequest;
import com.backend.dto.assistant.AssistantResponse;

public interface AssistantEngineService {

    AssistantResponse analyze(AssistantRequest request);

    ChatResponse chat(String message);
}