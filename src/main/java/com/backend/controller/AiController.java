package com.backend.controller;

import com.backend.service.impl.BedrockServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final BedrockServiceImpl bedrockService;

    public AiController(BedrockServiceImpl bedrockService) {
        this.bedrockService = bedrockService;
    }

    @GetMapping("/test")
    public String test(@RequestParam String prompt) {

        return bedrockService.askAI(prompt);

    }

}