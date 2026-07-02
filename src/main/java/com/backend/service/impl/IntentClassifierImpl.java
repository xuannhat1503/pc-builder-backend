package com.backend.service.impl;

import com.backend.dto.ai.AssistantIntent;
import com.backend.service.IntentClassifier;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class IntentClassifierImpl implements IntentClassifier {

    @Override
    public AssistantIntent classify(String message) {
        String text = normalize(message);

        if (containsAny(text, "cheaper", "reduced", "reduction", "replace cheaper", "replacements", "rẻ hơn", "re re hon", "alt", "alternatives")) {
            return AssistantIntent.RECOMMEND_CHEAPER_ALTERNATIVE;
        }

        if (containsAny(text, "upgrade", "upgrades", "nang cap", "nâng cấp")) {
            return AssistantIntent.RECOMMEND_BETTER_UPGRADE;
        }

        if (containsAny(text, "compare", "vs", "so sanh", "so sánh", "đối chiếu", "doi chieu")) {
            return AssistantIntent.COMPARE_COMPONENTS;
        }

        if (containsAny(text, "compatibility", "compatible", "tuong thich", "tương thích", "check compatibility", "kiem tra tuong thich")) {
            return AssistantIntent.CHECK_COMPATIBILITY;
        }

        if (containsAny(text, "what is", "is?", "là gì", "la gi", "explain", "giải thích", "giai thich")) {
            return AssistantIntent.EXPLAIN_COMPONENT;
        }

        if (containsAny(text, "gaming", "game", "photoshop", "premiere pro", "davinci resolve", "blender", "autocad", "revit", "sketchup", "solidworks", "unity", "unreal engine", "android studio", "visual studio", "docker", "vmware", "obs studio", "streamlabs", "excel", "power bi", "sql server", "oracle database", "stable diffusion", "ollama", "llama", "deepseek", "comfyui", "tensorflow", "pytorch", "local llm", "video editing")) {
            return AssistantIntent.RECOMMEND_BY_PURPOSE;
        }

        if (containsAny(text, "office", "van phong", "văn phòng", "programming", "coding", "hoc tap", "học tập", "render", "ai", "machine learning")) {
            return AssistantIntent.RECOMMEND_BY_PURPOSE;
        }

        if (containsAny(text, "amd", "intel", "nvidia", "corsair", "msi", "asus")) {
            return AssistantIntent.RECOMMEND_BY_BRAND;
        }

        if (containsAny(text, "budget", "ngan sach", "ngân sách", "under", "duoi", "dưới", "triệu", "trieu", "tr")) {
            return AssistantIntent.RECOMMEND_BY_BUDGET;
        }

        return AssistantIntent.NORMAL_CHAT;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }
}