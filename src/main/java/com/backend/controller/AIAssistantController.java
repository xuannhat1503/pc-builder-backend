package com.backend.controller;

import com.backend.dto.ai.ChatRequest;
import com.backend.dto.ai.ChatResponse;
import com.backend.service.AIAssistantService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;

    public AIAssistantController(AIAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        return aiAssistantService.chat(request.getMessage());

    }

}