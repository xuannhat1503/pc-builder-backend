package com.backend.service.impl;

import com.backend.dto.assistant.AssistantRequest;
import com.backend.dto.assistant.AssistantResponse;
import com.backend.service.AssistantEngineService;
import com.backend.service.SmartAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmartAssistantServiceImpl implements SmartAssistantService {

    private final AssistantEngineService assistantEngineService;

    @Override
    public AssistantResponse advise(AssistantRequest request) {
        return assistantEngineService.analyze(request);
    }
}