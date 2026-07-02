package com.backend.service.impl;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import com.backend.service.BedrockService;

@Service
public class BedrockServiceImpl implements BedrockService {

    // Claude Haiku 4.5 Inference Profile
    private static final String MODEL_ID =
            "us.anthropic.claude-haiku-4-5-20251001-v1:0";

    private final BedrockRuntimeClient client;

    public BedrockServiceImpl(BedrockRuntimeClient client) {
        this.client = client;
    }

    public String askAI(String prompt) {

        Message userMessage = Message.builder()
                .role(ConversationRole.USER)
                .content(
                        ContentBlock.builder()
                                .text(prompt)
                                .build()
                )
                .build();

        ConverseRequest request = ConverseRequest.builder()
                .modelId(MODEL_ID)
                .messages(userMessage)
                .build();

        ConverseResponse response = client.converse(request);

        return response.output()
                .message()
                .content()
                .get(0)
                .text();
    }

        @Override
        public String chat(String message) {
                return askAI(message);
        }
}