package com.backend.service;

import com.backend.dto.tavily.SearchResponse;
import com.backend.dto.tavily.SearchResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeSummaryPromptBuilder {

    public String buildPrompt(String userQuestion, String keyword, String category, SearchResponse searchResponse) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are summarizing Tavily search results into compact PC knowledge.\n");
        builder.append("Use only the supplied search results. Do not invent facts.\n");
        builder.append("Return Vietnamese only. No long paragraphs. No filler.\n");
        builder.append("Supported categories: GAME, SOFTWARE, AI_TOOL, FRAMEWORK, APPLICATION, WORKLOAD, BENCHMARK, CPU_INFO, GPU_INFO, TECHNOLOGY, OTHER.\n");
        builder.append("If the question is about gaming, extract: minimum/recommended CPU, GPU, RAM, storage, resolution, target FPS.\n");
        builder.append("If the question is about software/apps, extract: CPU priority, GPU priority, RAM, storage, recommended hardware, workload type.\n");
        builder.append("If the question is about AI tools/frameworks, extract: CPU, GPU/VRAM, RAM, storage, local inference notes, and whether GPU acceleration is important.\n");
        builder.append("If the question is about office/productivity, extract: CPU, RAM, storage, integrated vs discrete GPU guidance, and recommended resolution.\n");
        builder.append("If information is uncertain, mark it clearly as tham khảo.\n\n");
        builder.append("USER QUESTION:\n").append(nullToEmpty(userQuestion)).append("\n\n");
        builder.append("KEYWORD:\n").append(nullToEmpty(keyword)).append("\n\n");
        builder.append("CATEGORY:\n").append(nullToEmpty(category)).append("\n\n");
        builder.append("TAVILY RESULTS:\n");

        List<SearchResult> results = searchResponse == null ? List.of() : searchResponse.results();
        if (results == null || results.isEmpty()) {
            builder.append("No results found\n");
        } else {
            for (int i = 0; i < results.size(); i++) {
                SearchResult result = results.get(i);
                builder.append(i + 1).append(") ")
                        .append(nullToEmpty(result.title())).append(" | ")
                        .append(nullToEmpty(result.url())).append(" | ")
                        .append(firstNonBlank(result.rawContent(), result.content()))
                        .append("\n");
            }
        }

        builder.append("\nOUTPUT FORMAT:\n");
        builder.append("- Title: ...\n");
        builder.append("- Summary: ...\n");
        builder.append("- Minimum: ...\n");
        builder.append("- Recommended: ...\n");
        builder.append("- Notes: ...\n");
        return builder.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second == null ? "" : second;
    }
}