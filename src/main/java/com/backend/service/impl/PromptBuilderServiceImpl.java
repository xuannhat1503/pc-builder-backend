package com.backend.service.impl;

import com.backend.dto.ai.AiPromptContext;
import com.backend.dto.ai.AssistantIntent;
import com.backend.service.ComponentCatalogService;
import com.backend.service.ComponentFormatter;
import com.backend.service.PromptBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptBuilderServiceImpl implements PromptBuilderService {

    private static final String SYSTEM_INSTRUCTION = """
            You are an AI assistant for a PC Builder website.
            You must only answer using the supplied database data.
            Never invent products, brands, prices, or compatibility facts.
            If information is unavailable, answer exactly: I don't have enough data.
            For recommendation intents, return valid JSON only.
            For normal chat, return plain text only.
            """;

    private final ComponentCatalogService componentCatalogService;
    private final ComponentFormatter componentFormatter;

    @Override
    public String buildPrompt(AiPromptContext context) {
        String catalogText = context.catalogText();
        if (catalogText == null || catalogText.isBlank()) {
            catalogText = componentFormatter.formatCatalog(componentCatalogService.findAll());
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("SYSTEM INSTRUCTION:\n").append(SYSTEM_INSTRUCTION).append("\n\n");
        prompt.append("INTENT: ").append(context.intent() == null ? AssistantIntent.NORMAL_CHAT : context.intent()).append("\n\n");
        if (context.conversationContext() != null && !context.conversationContext().isBlank()) {
            prompt.append("CONVERSATION HISTORY:\n").append(context.conversationContext()).append("\n\n");
        }
        prompt.append("CURRENT DATABASE COMPONENTS:\n").append(catalogText).append("\n\n");
        prompt.append("CURRENT PRICES:\n").append(context.priceText() == null || context.priceText().isBlank() ? catalogText : context.priceText()).append("\n\n");
        prompt.append("USER QUESTION:\n").append(context.userQuestion() == null ? "" : context.userQuestion()).append("\n\n");

        if (context.recommendationJson() != null && !context.recommendationJson().isBlank()) {
            prompt.append("RECOMMENDATION CANDIDATE JSON:\n").append(context.recommendationJson()).append("\n\n");
        }

        if (context.comparisonJson() != null && !context.comparisonJson().isBlank()) {
            prompt.append("COMPARISON CANDIDATE JSON:\n").append(context.comparisonJson()).append("\n\n");
        }

        if (context.compatibilityJson() != null && !context.compatibilityJson().isBlank()) {
            prompt.append("COMPATIBILITY REPORT JSON:\n").append(context.compatibilityJson()).append("\n\n");
        }

        prompt.append("OUTPUT RULES:\n")
                .append("- Use only the supplied database data.\n")
                .append("- If data is insufficient, answer exactly: I don't have enough data.\n")
                .append("- Do not invent product names or prices.\n")
                .append("- If intent is recommendation, return JSON only.\n")
                .append("- If intent is normal chat, return plain text only.\n");

        return prompt.toString();
    }
}