package com.backend.service;

import com.backend.dto.assistant.AssistantRequest;
import com.backend.dto.assistant.AssistantResponse;

public interface SmartAssistantService {

    AssistantResponse advise(AssistantRequest request);
}