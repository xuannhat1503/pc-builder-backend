package com.backend.service.impl;

import com.backend.dto.ai.CatalogComponentView;
import com.backend.dto.ai.RecommendationResponse;
import com.backend.service.ClaudeClientService;
import com.backend.service.BuildRecommendationService;
import com.backend.service.ComponentCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildRecommendationServiceImpl implements BuildRecommendationService {

    private static final BigDecimal HEADROOM = BigDecimal.valueOf(120);

    private final ComponentCatalogService catalogService;
    private final ClaudeClientService claudeClientService;

    @Override
    public RecommendationResponse recommendByBudget(BigDecimal budget, String question) {
        return recommendInternal(question, budget, null, null);
    }

    @Override
    public RecommendationResponse recommendByPurpose(String purpose, BigDecimal budget, String question) {
        return recommendInternal(question, budget, purpose, null);
    }

    @Override
    public RecommendationResponse recommendByBrand(String brand, BigDecimal budget, String question) {
        return recommendInternal(question, budget, null, brand);
    }

    @Override
    public RecommendationResponse recommendCheaperAlternatives(String componentQuery) {
        CatalogComponentView base = findRequiredComponent(componentQuery);
        if (base == null) {
            return notEnoughData();
        }

        List<String> alternatives = catalogService.findAll().stream()
                .filter(item -> sameType(item, base))
                .filter(item -> item.lowestPrice() != null && base.lowestPrice() != null && item.lowestPrice().compareTo(base.lowestPrice()) < 0)
                .sorted(Comparator.comparing(CatalogComponentView::lowestPrice))
                .limit(3)
                .map(this::toShortLabel)
                .toList();

        return buildSingleComponentRecommendation(base, alternatives, List.of(), "Cheaper alternatives from database only.");
    }

    @Override
    public RecommendationResponse recommendBetterUpgrades(String componentQuery) {
        CatalogComponentView base = findRequiredComponent(componentQuery);
        if (base == null) {
            return notEnoughData();
        }

        List<String> upgrades = catalogService.findAll().stream()
                .filter(item -> sameType(item, base))
                .filter(item -> item.lowestPrice() != null && base.lowestPrice() != null && item.lowestPrice().compareTo(base.lowestPrice()) > 0)
                .sorted(Comparator.comparing(CatalogComponentView::lowestPrice).reversed())
                .limit(3)
                .map(this::toShortLabel)
                .toList();

        return buildSingleComponentRecommendation(base, List.of(), upgrades, "Better upgrades from database only.");
    }

    private RecommendationResponse recommendInternal(String question, BigDecimal budget, String purpose, String brand) {
        List<CatalogComponentView> catalog = catalogService.findAll();
        if (catalog.isEmpty()) {
            return notEnoughData();
        }

        String detectedPurpose = normalizePurpose(question, purpose);
        String detectedBrand = normalizeBrand(question, brand);
        BigDecimal parsedBudget = budget == null ? parseBudgetHint(question) : budget;

        List<CatalogComponentView> cpuPool = filterBrand(catalog, "CPU", detectedBrand);
        List<CatalogComponentView> mainboardPool = filterBrand(catalog, "MAINBOARD", detectedBrand);
        List<CatalogComponentView> ramPool = filterBrand(catalog, "RAM", detectedBrand);
        List<CatalogComponentView> gpuPool = filterBrand(catalog, "GPU", detectedBrand);
        List<CatalogComponentView> psuPool = filterBrand(catalog, "PSU", detectedBrand);

        CatalogComponentView cpu = chooseCpu(cpuPool, detectedPurpose);
        CatalogComponentView mainboard = chooseMainboard(mainboardPool, cpu, detectedPurpose);
        CatalogComponentView ram = chooseRam(ramPool, mainboard, detectedPurpose);
        CatalogComponentView gpu = chooseGpu(gpuPool, detectedPurpose);
        CatalogComponentView psu = choosePsu(psuPool, cpu, gpu);

        if (cpu == null || mainboard == null || ram == null || gpu == null || psu == null) {
            return notEnoughData();
        }

        BigDecimal total = sum(cpu, mainboard, ram, gpu, psu);
        if (parsedBudget != null && total.compareTo(parsedBudget) > 0) {
            CatalogComponentView cheaperGpu = chooseCheaperGpu(gpuPool, detectedPurpose, gpu);
            if (cheaperGpu != null) {
                gpu = cheaperGpu;
                psu = choosePsu(psuPool, cpu, gpu);
                total = sum(cpu, mainboard, ram, gpu, psu);
            }
        }

        String reason = buildReason(detectedPurpose, detectedBrand, parsedBudget, total);
        return new RecommendationResponse(
                cpu.name(),
                gpu.name(),
                ram.name(),
                mainboard.name(),
                psu.name(),
                total,
                reason,
                List.of(),
                List.of()
        );
    }

    private String buildReason(String purpose, String brand, BigDecimal budget, BigDecimal total) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are assisting a PC builder website. Use only the given database-derived facts. ");
        prompt.append("Do not invent products or prices. If data is insufficient, answer exactly: I don't have enough data.\n\n");
        prompt.append("Context:\n");
        prompt.append("- Purpose: ").append(purpose == null ? "general" : purpose).append("\n");
        prompt.append("- Brand focus: ").append(brand == null ? "none" : brand).append("\n");
        prompt.append("- Budget: ").append(budget == null ? "unknown" : budget).append("\n");
        prompt.append("- Estimated total price: ").append(total == null ? "unknown" : total).append("\n\n");
        prompt.append("Explain briefly why this build is recommended, in Vietnamese, based only on the above facts.");

        String answer = claudeClientService.ask(prompt.toString());
        if (answer == null || answer.isBlank()) {
            StringBuilder fallback = new StringBuilder("Cau hinh duoc chon tu du lieu trong database.");
            if (purpose != null && !purpose.isBlank()) {
                fallback.append(" Phu hop muc dich: ").append(purpose).append('.');
            }
            if (brand != null && !brand.isBlank()) {
                fallback.append(" Uu tien thuong hieu: ").append(brand).append('.');
            }
            if (budget != null) {
                fallback.append(" Tong gia nam trong muc ngan sach tham chieu.");
            }
            return fallback.toString();
        }

        return answer;
    }

    private RecommendationResponse buildSingleComponentRecommendation(CatalogComponentView base, List<String> alternatives, List<String> upgrades, String reason) {
        return new RecommendationResponse(
                "",
                "",
                "",
                "",
                "",
                base.lowestPrice(),
                reason,
                alternatives,
                upgrades
        );
    }

    private CatalogComponentView chooseCpu(List<CatalogComponentView> pool, String purpose) {
        if (pool.isEmpty()) {
            return null;
        }

        Comparator<CatalogComponentView> comparator = comparatorForPurpose(purpose, true);
        return pool.stream().sorted(comparator).findFirst().orElse(pool.get(0));
    }

    private CatalogComponentView chooseMainboard(List<CatalogComponentView> pool, CatalogComponentView cpu, String purpose) {
        if (pool.isEmpty()) {
            return null;
        }

        List<CatalogComponentView> compatible = pool.stream()
                .filter(item -> cpu == null || sameIgnoreCase(item.socketType(), cpu.socketType()))
                .toList();
        List<CatalogComponentView> candidates = compatible.isEmpty() ? pool : compatible;
        Comparator<CatalogComponentView> comparator = Comparator.comparing(CatalogComponentView::lowestPrice);
        if (isHighPerformancePurpose(purpose)) {
            comparator = comparator.reversed();
        }
        return candidates.stream().sorted(comparator).findFirst().orElse(candidates.get(0));
    }

    private CatalogComponentView chooseRam(List<CatalogComponentView> pool, CatalogComponentView mainboard, String purpose) {
        if (pool.isEmpty()) {
            return null;
        }

        List<CatalogComponentView> compatible = pool.stream()
                .filter(item -> mainboard == null || sameIgnoreCase(item.ramGeneration(), mainboard.ramGeneration()))
                .toList();
        List<CatalogComponentView> candidates = compatible.isEmpty() ? pool : compatible;
        int preferredCapacity = isHighPerformancePurpose(purpose) ? 32 : 16;
        return candidates.stream()
                .sorted(Comparator
                        .comparingInt((CatalogComponentView item) -> Math.abs((item.capacityGb() == null ? 0 : item.capacityGb()) - preferredCapacity))
                        .thenComparing(CatalogComponentView::lowestPrice))
                .findFirst()
                .orElse(candidates.get(0));
    }

    private CatalogComponentView chooseGpu(List<CatalogComponentView> pool, String purpose) {
        if (pool.isEmpty()) {
            return null;
        }

        Comparator<CatalogComponentView> comparator = Comparator
                .comparing((CatalogComponentView item) -> item.vramSizeGb() == null ? 0 : item.vramSizeGb())
                .thenComparing(item -> item.lowestPrice() == null ? BigDecimal.ZERO : item.lowestPrice());

        if (isHighPerformancePurpose(purpose)) {
            comparator = comparator.reversed();
        }

        return pool.stream().sorted(comparator).findFirst().orElse(pool.get(0));
    }

    private CatalogComponentView chooseCheaperGpu(List<CatalogComponentView> pool, String purpose, CatalogComponentView current) {
        if (current == null || pool.isEmpty()) {
            return null;
        }

        return pool.stream()
                .filter(item -> item.lowestPrice() != null && current.lowestPrice() != null && item.lowestPrice().compareTo(current.lowestPrice()) < 0)
                .sorted(Comparator.comparing(CatalogComponentView::lowestPrice))
                .findFirst()
                .orElse(null);
    }

    private CatalogComponentView choosePsu(List<CatalogComponentView> pool, CatalogComponentView cpu, CatalogComponentView gpu) {
        if (pool.isEmpty()) {
            return null;
        }

        int estimatedPower = (cpu == null || cpu.tdpWattage() == null ? 0 : cpu.tdpWattage())
                + (gpu == null || gpu.tdpWattage() == null ? 0 : gpu.tdpWattage())
                + HEADROOM.intValue();

        return pool.stream()
                .filter(item -> item.powerOutputWatt() != null && item.powerOutputWatt() >= estimatedPower)
            .sorted(Comparator
                .comparingInt((CatalogComponentView item) -> item.powerOutputWatt() == null ? Integer.MAX_VALUE : item.powerOutputWatt())
                .thenComparing(item -> item.lowestPrice() == null ? BigDecimal.ZERO : item.lowestPrice()))
                .findFirst()
                .orElseGet(() -> pool.stream()
                .sorted(Comparator
                    .comparingInt((CatalogComponentView item) -> item.powerOutputWatt() == null ? Integer.MAX_VALUE : item.powerOutputWatt())
                    .thenComparing(item -> item.lowestPrice() == null ? BigDecimal.ZERO : item.lowestPrice()))
                        .findFirst()
                        .orElse(pool.get(0)));
    }

    private Comparator<CatalogComponentView> comparatorForPurpose(String purpose, boolean cpu) {
        if (isHighPerformancePurpose(purpose)) {
            return Comparator.comparing(CatalogComponentView::lowestPrice).reversed();
        }

        return Comparator.comparing(CatalogComponentView::lowestPrice);
    }

    private boolean isHighPerformancePurpose(String purpose) {
        String text = normalize(purpose);
        return text.contains("gaming") || text.contains("render") || text.contains("ai") || text.contains("machine learning");
    }

    private String normalizePurpose(String question, String explicitPurpose) {
        if (explicitPurpose != null && !explicitPurpose.isBlank()) {
            return explicitPurpose.toLowerCase(Locale.ROOT);
        }
        String text = normalize(question);
        if (text.contains("gaming") || text.contains("game")) return "gaming";
        if (containsAny(text, "photoshop", "premiere pro", "davinci resolve", "blender", "autocad", "revit", "sketchup", "solidworks", "unity", "unreal engine", "obs studio", "streamlabs", "video editing")) return "rendering";
        if (containsAny(text, "stable diffusion", "ollama", "llama", "deepseek", "comfyui", "tensorflow", "pytorch", "local llm", "machine learning", "ai")) return "ai";
        if (containsAny(text, "android studio", "visual studio", "docker", "vmware")) return "programming";
        if (containsAny(text, "excel", "power bi", "sql server", "oracle database", "office", "van phong", "văn phòng")) return "office";
        if (text.contains("render")) return "rendering";
        if (text.contains("ai") || text.contains("machine learning")) return "ai";
        if (text.contains("office") || text.contains("van phong") || text.contains("văn phòng")) return "office";
        if (text.contains("programming") || text.contains("coding") || text.contains("code")) return "programming";
        return "general";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeBrand(String question, String explicitBrand) {
        if (explicitBrand != null && !explicitBrand.isBlank()) {
            return explicitBrand.trim();
        }

        String text = normalize(question);
        for (String brand : List.of("amd", "intel", "nvidia", "corsair", "msi", "asus")) {
            if (text.contains(brand)) {
                return brand;
            }
        }
        return null;
    }

    private BigDecimal parseBudgetHint(String question) {
        if (question == null || question.isBlank()) {
            return null;
        }

        String text = question.toLowerCase(Locale.ROOT);
        if (text.contains("25 million") || text.contains("25tr") || text.contains("25 trieu") || text.contains("25 triệu")) {
            return BigDecimal.valueOf(25_000_000L);
        }
        if (text.contains("30 million") || text.contains("30tr") || text.contains("30 trieu") || text.contains("30 triệu")) {
            return BigDecimal.valueOf(30_000_000L);
        }
        return null;
    }

    private List<CatalogComponentView> filterBrand(List<CatalogComponentView> all, String type, String brand) {
        List<CatalogComponentView> typed = all.stream()
                .filter(item -> sameIgnoreCase(item.type(), type))
                .toList();
        if (brand == null || brand.isBlank()) {
            return typed;
        }

        List<CatalogComponentView> branded = typed.stream()
                .filter(item -> item.brand() != null && item.brand().toLowerCase(Locale.ROOT).contains(brand.toLowerCase(Locale.ROOT)))
                .toList();
        return branded.isEmpty() ? typed : branded;
    }

    private CatalogComponentView findRequiredComponent(String componentQuery) {
        return catalogService.findBestMatch(componentQuery).orElse(null);
    }

    private boolean sameType(CatalogComponentView left, CatalogComponentView right) {
        return sameIgnoreCase(left.type(), right.type());
    }

    private boolean sameIgnoreCase(String left, String right) {
        return left == null ? right == null : left.equalsIgnoreCase(right);
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    private String toShortLabel(CatalogComponentView view) {
        return "%s | %s | %s | %s".formatted(view.type(), view.name(), view.lowestPrice(), view.cheapestSource());
    }

    private BigDecimal sum(CatalogComponentView... components) {
        BigDecimal total = BigDecimal.ZERO;
        for (CatalogComponentView component : components) {
            if (component != null && component.lowestPrice() != null) {
                total = total.add(component.lowestPrice());
            }
        }
        return total.setScale(0, RoundingMode.HALF_UP);
    }

    private RecommendationResponse notEnoughData() {
        return new RecommendationResponse(null, null, null, null, null, BigDecimal.ZERO, "I don't have enough data.", List.of(), List.of());
    }
}