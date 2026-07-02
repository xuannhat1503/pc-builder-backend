package com.backend.service.impl;

import com.backend.dto.ai.AiPromptContext;
import com.backend.dto.ai.AssistantIntent;
import com.backend.dto.ai.CatalogComponentView;
import com.backend.dto.ai.ChatResponse;
import com.backend.dto.ai.ComparisonResponse;
import com.backend.dto.ai.RecommendationResponse;
import com.backend.dto.assistant.AssistantRequest;
import com.backend.dto.assistant.AssistantResponse;
import com.backend.dto.knowledge.KnowledgeBaseRequest;
import com.backend.dto.compatibility.CompatibilityReport;
import com.backend.dto.price.PriceSummaryReport;
import com.backend.service.AssistantEngineService;
import com.backend.service.BudgetParser;
import com.backend.service.BuildRecommendationService;
import com.backend.service.ClaudeClientService;
import com.backend.service.ComponentCatalogService;
import com.backend.service.ComponentFormatter;
import com.backend.service.CompatibilityService;
import com.backend.service.ComparisonService;
import com.backend.service.IntentClassifier;
import com.backend.service.KnowledgeBaseService;
import com.backend.service.KnowledgeLearningService;
import com.backend.service.PriceInsightService;
import com.backend.service.PromptBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssistantEngineServiceImpl implements AssistantEngineService {

    private final IntentClassifier intentClassifier;
    private final BudgetParser budgetParser;
    private final ComponentCatalogService catalogService;
    private final ComponentFormatter componentFormatter;
    private final BuildRecommendationService recommendationService;
    private final ComparisonService comparisonService;
    private final CompatibilityService compatibilityService;
    private final PriceInsightService priceInsightService;
    private final PromptBuilderService promptBuilderService;
    private final ClaudeClientService claudeClientService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeLearningService knowledgeLearningService;

    @Override
    public AssistantResponse analyze(AssistantRequest request) {
        String message = request.message() == null ? "" : request.message().trim();
        String conversationContext = request.conversationContext() == null ? "" : request.conversationContext().trim();
        AssistantIntent intent = intentClassifier.classify(message);
        List<String> recommendations = new ArrayList<>();
        CompatibilityReport compatibility = null;
        PriceSummaryReport priceSummary = null;
        String answer;

        if (request.compatibilityComponents() != null && !request.compatibilityComponents().isEmpty()) {
            List<Long> ids = request.compatibilityComponents().stream()
                    .map(item -> item.componentId())
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .toList();
            BigDecimal totalPrice = request.budget();
            compatibility = compatibilityService.checkCompatibility(ids, totalPrice);
            recommendations.addAll(compatibility.suggestions());
        }

        if (request.componentId() != null) {
            priceSummary = priceInsightService.summarizeComponentPrice(request.componentId());
            recommendations.add("Nguon gia re nhat hien tai: " + priceSummary.cheapestSource());
            recommendations.add("Gia trung binh: " + priceSummary.averagePrice());
        }

        List<String> dbRecommendations = new ArrayList<>();

        if (isGameRunCheckQuery(message) && isBuildRequestForGame(message)) {
            BigDecimal budget = budgetParser.parseBudget(message).orElse(request.budget());
            String keyword = extractGameKeyword(message);
            String gameRequirement = knowledgeLearningService.learn(
                    keyword,
                    "game_requirement",
                    message,
                    conversationContext
            ).content();
            RecommendationResponse recommendation = recommendationService.recommendByPurpose("gaming", budget, message);
            String reason = buildGameBuildReason(message, conversationContext, recommendation, gameRequirement);
            answer = toJsonWithReason(recommendation, reason);
            dbRecommendations.add("Mục đích phát hiện: gaming");
            return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
        }

        if (isGameRunCheckQuery(message)) {
            String answerHybrid = buildHybridGameCheckAnswer(message, conversationContext, request, compatibility);
            return new AssistantResponse(answerHybrid, finishRecommendations(recommendations, List.of()), compatibility, priceSummary);
        }

        String prompt;

        switch (intent) {
            case RECOMMEND_BY_BUDGET -> {
                BigDecimal budget = budgetParser.parseBudget(message).orElse(request.budget());
                RecommendationResponse recommendation = recommendationService.recommendByBudget(budget, message);
                answer = toJsonWithReason(recommendation, buildClaudeReason(intent, message, recommendation, compatibility, null));
                dbRecommendations.add("Ngân sách phát hiện: " + (budget == null ? "không xác định" : budget));
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
            case RECOMMEND_BY_PURPOSE -> {
                BigDecimal budget = budgetParser.parseBudget(message).orElse(request.budget());
                String purpose = detectPurpose(message);
                String knowledgeCategory = detectKnowledgeCategory(message, purpose);
                String knowledgeKeyword = extractKnowledgeKeyword(message, purpose);
                learnKnowledgeIfNeeded(knowledgeKeyword, knowledgeCategory, message, conversationContext);
                RecommendationResponse recommendation = recommendationService.recommendByPurpose(purpose, budget, message);
                answer = toJsonWithReason(recommendation, buildClaudeReason(intent, message, recommendation, compatibility, null));
                dbRecommendations.add("Mục đích phát hiện: " + purpose);
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
            case RECOMMEND_BY_BRAND -> {
                BigDecimal budget = budgetParser.parseBudget(message).orElse(request.budget());
                String brand = detectBrand(message);
                RecommendationResponse recommendation = recommendationService.recommendByBrand(brand, budget, message);
                answer = toJsonWithReason(recommendation, buildClaudeReason(intent, message, recommendation, compatibility, null));
                dbRecommendations.add("Brand phát hiện: " + brand);
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
            case RECOMMEND_CHEAPER_ALTERNATIVE -> {
                RecommendationResponse recommendation = recommendationService.recommendCheaperAlternatives(message);
                answer = toJsonWithReason(recommendation, buildClaudeReason(intent, message, recommendation, compatibility, null));
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
            case RECOMMEND_BETTER_UPGRADE -> {
                RecommendationResponse recommendation = recommendationService.recommendBetterUpgrades(message);
                answer = toJsonWithReason(recommendation, buildClaudeReason(intent, message, recommendation, compatibility, null));
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
            case COMPARE_COMPONENTS -> {
                String[] queries = extractComparisonQueries(message);
                ComparisonResponse comparison = comparisonService.compare(queries[0], queries[1]);
                prompt = promptBuilderService.buildPrompt(new AiPromptContext(
                        intent,
                        message,
                    conversationContext,
                        componentFormatter.formatCatalog(catalogService.findAll()),
                        componentFormatter.formatCatalog(catalogService.findAll()),
                        null,
                    toJsonSafely(comparison),
                    compatibility == null ? null : toJsonSafely(compatibility)
                ));
                String explain = claudeClientService.ask(prompt);
                answer = explain == null || explain.isBlank() ? comparison.reason() : explain;
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
            case CHECK_COMPATIBILITY -> {
                if (compatibility == null) {
                    answer = "I don't have enough data.";
                    return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), null, priceSummary);
                }
                prompt = promptBuilderService.buildPrompt(new AiPromptContext(
                        intent,
                        message,
                    conversationContext,
                        componentFormatter.formatCatalog(catalogService.findAll()),
                        componentFormatter.formatCatalog(catalogService.findAll()),
                        null,
                        null,
                    toJsonSafely(compatibility)
                ));
                String explain = claudeClientService.ask(prompt);
                answer = explain == null || explain.isBlank() ? String.join(" | ", compatibility.suggestions()) : explain;
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
            case EXPLAIN_COMPONENT -> {
                if (request.componentId() == null) {
                    answer = "I don't have enough data.";
                    return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
                }
                String componentText = componentCatalogTextForId(request.componentId());
                prompt = promptBuilderService.buildPrompt(new AiPromptContext(
                        intent,
                        message,
                    conversationContext,
                        componentText,
                        componentText,
                        null,
                        null,
                        compatibility == null ? null : toJsonSafely(compatibility)
                ));
                String explain = claudeClientService.ask(prompt);
                answer = explain == null || explain.isBlank() ? "I don't have enough data." : explain;
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
            case NORMAL_CHAT -> {
                prompt = promptBuilderService.buildPrompt(new AiPromptContext(
                        intent,
                        message,
                    conversationContext,
                        componentFormatter.formatCatalog(catalogService.findAll()),
                        componentFormatter.formatCatalog(catalogService.findAll()),
                        null,
                        null,
                    compatibility == null ? null : toJsonSafely(compatibility)
                ));
                String text = claudeClientService.ask(prompt);
                answer = text == null || text.isBlank() ? "I don't have enough data." : text;
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
            default -> {
                prompt = promptBuilderService.buildPrompt(new AiPromptContext(
                        intent,
                        message,
                    conversationContext,
                        componentFormatter.formatCatalog(catalogService.findAll()),
                        componentFormatter.formatCatalog(catalogService.findAll()),
                        null,
                        null,
                    compatibility == null ? null : toJsonSafely(compatibility)
                ));
                String text = claudeClientService.ask(prompt);
                answer = text == null || text.isBlank() ? "I don't have enough data." : text;
                return new AssistantResponse(answer, finishRecommendations(recommendations, dbRecommendations), compatibility, priceSummary);
            }
        }
    }

    @Override
    public ChatResponse chat(String message) {
        AssistantRequest request = new AssistantRequest(message, null, null, null, List.of(), null);
        AssistantResponse response = analyze(request);
        return new ChatResponse(response.answer());
    }

    private String buildClaudeReason(AssistantIntent intent, String message, RecommendationResponse recommendation, CompatibilityReport compatibility, PriceSummaryReport priceSummary) {
        String prompt = promptBuilderService.buildPrompt(new AiPromptContext(
                intent,
                message,
                null,
                componentFormatter.formatCatalog(catalogService.findAll()),
                componentFormatter.formatCatalog(catalogService.findAll()),
                toJsonSafely(recommendation),
                null,
                compatibility == null ? null : toJsonSafely(compatibility)
        ));
        String explanation = claudeClientService.ask(prompt);
        return explanation == null || explanation.isBlank() ? recommendation.reason() : explanation;
    }

    private String toJsonSafely(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof RecommendationResponse recommendation) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("cpu", recommendation.cpu());
            payload.put("gpu", recommendation.gpu());
            payload.put("ram", recommendation.ram());
            payload.put("mainboard", recommendation.mainboard());
            payload.put("psu", recommendation.psu());
            payload.put("estimatedPrice", recommendation.estimatedPrice());
            payload.put("reason", recommendation.reason());
            payload.put("cheaperAlternatives", recommendation.cheaperAlternatives());
            payload.put("betterUpgrades", recommendation.betterUpgrades());
            return toJsonMap(payload);
        }
        if (value instanceof ComparisonResponse comparison) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("left", comparison.left());
            payload.put("right", comparison.right());
            payload.put("verdict", comparison.verdict());
            payload.put("reason", comparison.reason());
            payload.put("differences", comparison.differences());
            return toJsonMap(payload);
        }
        if (value instanceof CompatibilityReport compatibility) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("buildId", compatibility.buildId());
            payload.put("compatible", compatibility.compatible());
            payload.put("cpuSocketType", compatibility.cpuSocketType());
            payload.put("mainboardSocketType", compatibility.mainboardSocketType());
            payload.put("ramGeneration", compatibility.ramGeneration());
            payload.put("estimatedPowerWatt", compatibility.estimatedPowerWatt());
            payload.put("recommendedPsuWatt", compatibility.recommendedPsuWatt());
            payload.put("totalPrice", compatibility.totalPrice());
            payload.put("issues", compatibility.issues());
            payload.put("suggestions", compatibility.suggestions());
            return toJsonMap(payload);
        }
        return "\"" + escapeJson(String.valueOf(value)) + "\"";
    }

    private String toJsonMap(Map<String, ?> map) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escapeJson(entry.getKey())).append('"').append(':').append(toJsonValue(entry.getValue()));
        }
        builder.append('}');
        return builder.toString();
    }

    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + escapeJson(string) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof List<?> list) {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append(toJsonValue(list.get(i)));
            }
            builder.append(']');
            return builder.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder builder = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append('"').append(escapeJson(String.valueOf(entry.getKey()))).append('"').append(':').append(toJsonValue(entry.getValue()));
            }
            builder.append('}');
            return builder.toString();
        }
        return "\"" + escapeJson(String.valueOf(value)) + "\"";
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String componentCatalogTextForId(Long componentId) {
        return catalogService.findAll().stream()
                .filter(item -> item.componentId() != null && item.componentId().equals(componentId))
                .findFirst()
                .map(item -> item.promptLine())
                .orElse(componentFormatter.formatCatalog(catalogService.findAll()));
    }

    private String toJsonWithReason(RecommendationResponse recommendation, String reason) {
        RecommendationResponse finalResponse = new RecommendationResponse(
                recommendation.cpu(),
                recommendation.gpu(),
                recommendation.ram(),
                recommendation.mainboard(),
                recommendation.psu(),
                recommendation.estimatedPrice(),
                reason,
                recommendation.cheaperAlternatives(),
                recommendation.betterUpgrades()
        );
        return toJsonSafely(finalResponse);
    }

    private List<String> finishRecommendations(List<String> existing, List<String> generated) {
        if ((generated == null || generated.isEmpty()) && (existing == null || existing.isEmpty())) {
            return List.of();
        }

        List<String> merged = new ArrayList<>();
        if (existing != null) merged.addAll(existing);
        if (generated != null) merged.addAll(generated);
        return Collections.unmodifiableList(merged);
    }

    private String detectPurpose(String message) {
        String text = normalize(message);
        if (text.contains("gaming") || text.contains("game")) return "gaming";
        if (containsAny(text, "photoshop", "premiere pro", "davinci resolve", "blender", "autocad", "revit", "sketchup", "solidworks", "unity", "unreal engine", "obs studio", "streamlabs", "video editing")) return "rendering";
        if (containsAny(text, "stable diffusion", "ollama", "llama", "deepseek", "comfyui", "tensorflow", "pytorch", "local llm", "machine learning", "ai")) return "ai";
        if (containsAny(text, "android studio", "visual studio", "docker", "vmware", "programming", "coding", "code")) return "programming";
        if (containsAny(text, "excel", "power bi", "sql server", "oracle database", "office", "van phong", "văn phòng", "office work")) return "office";
        if (text.contains("render")) return "rendering";
        if (text.contains("ai") || text.contains("machine learning")) return "ai";
        if (text.contains("office") || text.contains("van phong") || text.contains("văn phòng")) return "office";
        if (text.contains("programming") || text.contains("coding") || text.contains("code")) return "programming";
        return "general";
    }

    private String detectKnowledgeCategory(String message, String purpose) {
        String text = normalize(message);
        if (text.contains("game") || text.contains("gaming")) return "GAME";
        if (containsAny(text, "photoshop", "premiere pro", "davinci resolve", "blender", "autocad", "revit", "sketchup", "solidworks", "unity", "unreal engine", "obs studio", "streamlabs", "video editing")) {
            return "SOFTWARE";
        }
        if (containsAny(text, "stable diffusion", "ollama", "llama", "deepseek", "comfyui", "tensorflow", "pytorch", "local llm")) {
            return "AI_TOOL";
        }
        if (containsAny(text, "android studio", "visual studio", "docker", "vmware")) {
            return "APPLICATION";
        }
        if (containsAny(text, "excel", "power bi", "sql server", "oracle database", "office", "van phong", "văn phòng")) {
            return "WORKLOAD";
        }
        if (containsAny(text, "intel", "amd", "nvidia", "cpu", "gpu", "ram", "psu", "mainboard")) {
            return "TECHNOLOGY";
        }
        if (purpose != null && !purpose.isBlank()) {
            return purpose.equalsIgnoreCase("rendering") || purpose.equalsIgnoreCase("ai") ? "WORKLOAD" : "OTHER";
        }
        return "OTHER";
    }

    private String extractKnowledgeKeyword(String message, String purpose) {
        String text = normalize(message);
        for (String keyword : List.of(
                "photoshop", "premiere pro", "davinci resolve", "blender", "autocad", "revit", "sketchup", "solidworks",
                "unity", "unreal engine", "android studio", "visual studio", "docker", "vmware", "obs studio",
                "streamlabs", "excel", "power bi", "sql server", "oracle database", "stable diffusion", "ollama",
                "llama", "deepseek", "comfyui", "tensorflow", "pytorch", "local llm", "gta vi", "cyberpunk", "black myth wukong"
        )) {
            if (text.contains(keyword)) {
                return keyword;
            }
        }
        if (purpose != null && !purpose.isBlank()) {
            return purpose;
        }
        return message == null ? "" : message.trim();
    }

    private void learnKnowledgeIfNeeded(String keyword, String category, String message, String conversationContext) {
        if (keyword == null || keyword.isBlank() || category == null || category.isBlank()) {
            return;
        }
        try {
            knowledgeLearningService.learn(keyword, category, message, conversationContext);
        } catch (Exception exception) {
            log.warn("[Knowledge] Learning skipped for keyword={} category={}", keyword, category, exception);
        }
    }

    private String detectBrand(String message) {
        String text = normalize(message);
        for (String brand : List.of("amd", "intel", "nvidia", "corsair", "msi", "asus")) {
            if (text.contains(brand)) {
                return brand;
            }
        }
        return null;
    }

    private String[] extractComparisonQueries(String message) {
        String normalized = normalize(message)
                .replace("so sanh", " vs ")
                .replace("so sánh", " vs ")
                .replace("compare", " vs ")
                .replace("đối chiếu", " vs ")
                .replace("doi chieu", " vs ");

        String[] parts = normalized.split("\\s+vs\\s+|\\s+with\\s+|\\s+and\\s+|\\s+va\\s+|\\s+và\\s+");
        if (parts.length >= 2) {
            return new String[]{parts[0].trim(), parts[1].trim()};
        }

        List<String> catalogNames = catalogService.findAll().stream().map(view -> view.name().toLowerCase(Locale.ROOT)).toList();
        if (catalogNames.size() >= 2) {
            return new String[]{catalogNames.get(0), catalogNames.get(1)};
        }
        return new String[]{message, message};
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private boolean isGameRunCheckQuery(String message) {
        String text = normalize(message);
        if (!containsAny(text,
                "choi duoc", "chơi được", "can run", "run game", "fps", "minimum requirement",
                "recommended requirement", "system requirement", "cau hinh toi thieu", "cấu hình tối thiểu",
                "cau hinh de nghi", "cấu hình đề nghị")) {
            return false;
        }

        return !containsAny(text, "cpu", "gpu", "mainboard", "ram", "psu", "socket", "vram");
    }

    private boolean isBuildRequestForGame(String message) {
        String text = normalize(message);
        return containsAny(text,
                "build", "bo pc", "bộ pc", "cau hinh", "cấu hình", "lap may", "lắp máy", "goi y", "gợi ý");
    }

    private String buildHybridGameCheckAnswer(String message,
                                               String conversationContext,
                                               AssistantRequest request,
                                               CompatibilityReport compatibility) {
        String gameRequirement = extractGameRequirementFromAi(message, conversationContext);

        List<Long> componentIds = request.compatibilityComponents() == null
                ? List.of()
                : request.compatibilityComponents().stream()
                .map(AssistantRequest.CompatibilityItem::componentId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        if (componentIds.isEmpty()) {
            return "Mình đã lấy yêu cầu game ở mức tham khảo từ AI, nhưng bạn chưa đính kèm build để đối chiếu với dữ liệu linh kiện trong hệ thống. "
                    + "Bạn chọn build hoặc workspace trước, mình sẽ kiểm tra ngay.\n\n"
                    + "Yêu cầu game tham khảo: " + gameRequirement;
        }

        String buildText = buildCatalogLinesByIds(componentIds);
        String compatibilityText = compatibility == null ? "không có" : toJsonSafely(compatibility);

        String comparePrompt = """
                Bạn là trợ lý tư vấn PC.
                Nhiệm vụ:
                1) Dùng phần YEU_CAU_GAME_THAM_KHAO để hiểu game cần gì.
                2) Dùng phần BUILD_DB bên dưới (đây là linh kiện thật từ database) để đối chiếu.
                3) Trả lời ngắn gọn bằng tiếng Việt, thân thiện, theo đúng mẫu:
                   - Kết luận: ...
                   - Điểm đạt: ...
                   - Điểm cần nâng cấp: ...
                   - Gợi ý tiếp theo: ...
                4) Không bịa linh kiện ngoài BUILD_DB.
                5) Nếu thiếu dữ liệu, nói rõ thiếu gì.

                CAU_HOI_NGUOI_DUNG:
                %s

                YEU_CAU_GAME_THAM_KHAO:
                %s

                BUILD_DB:
                %s

                BAO_CAO_TUONG_THICH_NOI_BO:
                %s
                """.formatted(message, gameRequirement, buildText, compatibilityText);

        String compared = claudeClientService.ask(comparePrompt);
        if (compared == null || compared.isBlank()) {
            return "Mình đã lấy yêu cầu game tham khảo và đã có build trong hệ thống, nhưng chưa tổng hợp được kết quả so sánh. Bạn thử gửi lại câu hỏi một lần nữa giúp mình.";
        }
        String normalizedCompared = compared.trim();
        if (containsAny(normalize(normalizedCompared), "not in my database", "i don't have enough data", "cannot accurately match")) {
            return "Mình đã đối chiếu build hiện tại với dữ liệu linh kiện trong hệ thống. Với game này, dữ liệu yêu cầu chính thức còn thiếu nên kết quả chỉ ở mức tham khảo. Nếu bạn muốn, mình sẽ gợi ý ngay một cấu hình gaming an toàn theo ngân sách hiện tại.";
        }
        return normalizedCompared;
    }

    private String buildGameBuildReason(String message, String conversationContext, RecommendationResponse recommendation) {
        String gameRequirement = knowledgeLearningService.learn(
                extractGameKeyword(message),
                "game_requirement",
                message,
                conversationContext
        ).content();
        return buildGameBuildReason(message, conversationContext, recommendation, gameRequirement);
    }

    private String buildGameBuildReason(String message, String conversationContext, RecommendationResponse recommendation, String gameRequirement) {
        String prompt = """
                Bạn là trợ lý tư vấn PC.
                Người dùng hỏi build để chơi một game cụ thể.
                Hãy viết giải thích tiếng Việt tự nhiên, ngắn gọn (3-5 câu), thân thiện.
                Bắt buộc:
                - Không nói "không có dữ liệu" theo kiểu từ chối hoàn toàn.
                - Nêu rõ đây là cấu hình gaming tham khảo dựa trên linh kiện đang có trong DB.
                - Nhắc nếu cần xác nhận thêm mức FPS/độ phân giải.

                CAU_HOI:
                %s

                YEU_CAU_GAME_THAM_KHAO:
                %s

                CAU_HINH_GOI_Y_DB:
                %s
                """.formatted(message, gameRequirement, toJsonSafely(recommendation));

        String explanation = claudeClientService.ask(prompt);
        if (explanation == null || explanation.isBlank()) {
            return "Cấu hình này là phương án gaming tham khảo từ dữ liệu linh kiện hiện có trong hệ thống, ưu tiên cân bằng hiệu năng và chi phí. Nếu bạn cho mình thêm mục tiêu FPS và độ phân giải, mình sẽ tinh chỉnh sát hơn.";
        }

        String text = explanation.trim();
        if (containsAny(normalize(text), "i don't have enough data", "not in my database", "cannot accurately match")) {
            return "Cấu hình này là phương án gaming tham khảo từ dữ liệu linh kiện hiện có trong hệ thống, ưu tiên cân bằng hiệu năng và chi phí. Nếu bạn cho mình thêm mục tiêu FPS và độ phân giải, mình sẽ tinh chỉnh sát hơn.";
        }
        return text;
    }

    private Optional<String> findCachedKnowledge(String keyword, String category) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedCategory = normalizeKeyword(category);
        if (normalizedKeyword.isBlank() && normalizedCategory.isBlank()) {
            return Optional.empty();
        }
        return knowledgeBaseService.findMostRelevant(normalizedKeyword, normalizedCategory).map(saved -> {
            knowledgeBaseService.incrementAccess(saved.id());
            return saved.content();
        });
    }

    private String extractGameKeyword(String message) {
        String text = message == null ? "" : message.trim();
        String lowered = text.toLowerCase(Locale.ROOT);
        String[] markers = {"for ", "choi ", "chơi ", "build cho toi bo pc choi duoc ", "build cho toi bo pc choi duoc", "build a pc for "};
        for (String marker : markers) {
            int index = lowered.indexOf(marker);
            if (index >= 0) {
                String candidate = text.substring(index + marker.length()).trim();
                if (!candidate.isBlank()) {
                    return candidate.replaceAll("[?.!]+$", "").trim();
                }
            }
        }
            String cleaned = text
                .replaceAll("(?i)^game\\s+", "")
                .replaceAll("(?i)\\b(có chơi được không|co choi duoc khong|chơi được không|choi duoc khong|có build được không|co build duoc khong|build được không|build duoc khong|cấu hình tối thiểu|cau hinh toi thieu|minimum requirement|system requirement|recommended requirement|cấu hình đề nghị|cau hinh de nghi)\\b.*$", "")
                .replaceAll("(?i)\\b(build|pc|bo may|bộ máy|bộ pc|choi duoc|chơi được)\\b", "")
                .replaceAll("\\s+", " ")
                .trim();
            return cleaned.isBlank() ? text.trim() : cleaned;
    }

    private String normalizeKeyword(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String extractGameRequirementFromAi(String message, String conversationContext) {
        return knowledgeLearningService.learn(
                extractGameKeyword(message),
                "game_requirement",
                message,
                conversationContext
        ).content();
    }

    private String buildCatalogLinesByIds(List<Long> componentIds) {
        Map<Long, String> map = catalogService.findAll().stream()
                .filter(view -> view.componentId() != null)
                .collect(Collectors.toMap(CatalogComponentView::componentId, CatalogComponentView::promptLine, (a, b) -> a, LinkedHashMap::new));

        String lines = componentIds.stream()
                .map(id -> map.getOrDefault(id, "UNKNOWN | componentId=" + id))
                .collect(Collectors.joining("\n"));
        return lines.isBlank() ? "Không có dữ liệu linh kiện trong DB cho build này." : lines;
    }
}