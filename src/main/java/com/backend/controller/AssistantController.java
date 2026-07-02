package com.backend.controller;

import com.backend.dto.assistant.AssistantRequest;
import com.backend.dto.assistant.AssistantResponse;
import com.backend.service.SmartAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assistant")
public class AssistantController {

    private final SmartAssistantService service;

    @PostMapping("/analyze")
    public ResponseEntity<AssistantResponse> analyze(@Valid @RequestBody AssistantRequest request) {
        return ResponseEntity.ok(service.advise(request));
    }
}