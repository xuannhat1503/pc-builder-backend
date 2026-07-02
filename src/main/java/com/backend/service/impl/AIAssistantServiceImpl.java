package com.backend.service.impl;

import com.backend.dto.ai.ChatResponse;
import com.backend.service.AssistantEngineService;
import com.backend.service.AIAssistantService;
import org.springframework.stereotype.Service;

@Service
public class AIAssistantServiceImpl implements AIAssistantService {

    private final AssistantEngineService assistantEngineService;

    public AIAssistantServiceImpl(AssistantEngineService assistantEngineService) {
        this.assistantEngineService = assistantEngineService;
    }

    @Override
    public ChatResponse chat(String message) {
        return assistantEngineService.chat(message);
    }
}