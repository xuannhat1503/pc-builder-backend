package com.backend.service.impl;

import com.backend.service.BedrockService;
import com.backend.service.ClaudeClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClaudeClientServiceImpl implements ClaudeClientService {

    private final BedrockService bedrockService;

    @Override
    public String ask(String prompt) {
        return bedrockService.chat(prompt);
    }
}