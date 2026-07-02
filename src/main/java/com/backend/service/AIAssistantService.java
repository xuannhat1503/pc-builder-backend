package com.backend.service;

import com.backend.dto.ai.ChatResponse;

public interface AIAssistantService {

    ChatResponse chat(String message);

}