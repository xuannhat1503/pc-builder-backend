package com.backend.service;

import com.backend.dto.ai.AssistantIntent;

public interface IntentClassifier {

    AssistantIntent classify(String message);
}