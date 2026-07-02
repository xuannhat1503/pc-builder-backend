package com.backend.service.impl;

import com.backend.dto.ai.CatalogComponentView;
import com.backend.entity.Cpu;
import com.backend.entity.Gpu;
import com.backend.entity.Mainboard;
import com.backend.entity.Psu;
import com.backend.entity.Ram;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.CpuRepository;
import com.backend.repository.GpuRepository;
import com.backend.repository.MainboardRepository;
import com.backend.repository.PsuRepository;
import com.backend.repository.RamRepository;
import com.backend.service.ComponentCatalogService;
import com.backend.service.ComponentFormatter;
import com.backend.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComponentCatalogServiceImpl implements ComponentCatalogService {

    private final CpuRepository cpuRepository;
    private final MainboardRepository mainboardRepository;
    private final RamRepository ramRepository;
    private final GpuRepository gpuRepository;
    private final PsuRepository psuRepository;
    private final PriceService priceService;
    private final ComponentFormatter componentFormatter;

    @Override
    public List<CatalogComponentView> findAll() {
        List<CatalogComponentView> catalog = new ArrayList<>();
        catalog.addAll(formatCpus());
        catalog.addAll(formatMainboards());
        catalog.addAll(formatRams());
        catalog.addAll(formatGpus());
        catalog.addAll(formatPsus());
        return catalog.stream()
                .sorted(Comparator.comparing(CatalogComponentView::type).thenComparing(CatalogComponentView::lowestPrice))
                .toList();
    }

    @Override
    public Map<String, List<CatalogComponentView>> groupByType() {
        return findAll().stream().collect(Collectors.groupingBy(CatalogComponentView::type));
    }

    @Override
    public List<CatalogComponentView> findByBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            return List.of();
        }

        String keyword = brand.toLowerCase(Locale.ROOT).trim();
        return findAll().stream()
                .filter(item -> item.brand() != null && item.brand().toLowerCase(Locale.ROOT).contains(keyword))
                .toList();
    }

    @Override
    public List<CatalogComponentView> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String keyword = query.toLowerCase(Locale.ROOT).trim();
        return findAll().stream()
                .map(item -> new SearchScore(item, score(item, keyword)))
                .filter(score -> score.value() > 0)
                .sorted(Comparator.<SearchScore>comparingInt(SearchScore::value).reversed()
                        .thenComparing(score -> score.view().lowestPrice()))
                .limit(Math.max(limit, 1))
                .map(SearchScore::view)
                .toList();
    }

    @Override
    public Optional<CatalogComponentView> findBestMatch(String query) {
        return search(query, 1).stream().findFirst();
    }

    private List<CatalogComponentView> formatCpus() {
        return cpuRepository.findAll().stream()
                .map(this::safeFormatCpu)
                .flatMap(Optional::stream)
                .toList();
    }

    private List<CatalogComponentView> formatMainboards() {
        return mainboardRepository.findAll().stream()
                .map(this::safeFormatMainboard)
                .flatMap(Optional::stream)
                .toList();
    }

    private List<CatalogComponentView> formatRams() {
        return ramRepository.findAll().stream()
                .map(this::safeFormatRam)
                .flatMap(Optional::stream)
                .toList();
    }

    private List<CatalogComponentView> formatGpus() {
        return gpuRepository.findAll().stream()
                .map(this::safeFormatGpu)
                .flatMap(Optional::stream)
                .toList();
    }

    private List<CatalogComponentView> formatPsus() {
        return psuRepository.findAll().stream()
                .map(this::safeFormatPsu)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<CatalogComponentView> safeFormatCpu(Cpu cpu) {
        try {
            return Optional.of(componentFormatter.format(cpu, priceService.getLowestPrice(cpu.getId()), priceService.getCheapestSourceName(cpu.getId())));
        } catch (ResourceNotFoundException ex) {
            return Optional.empty();
        }
    }

    private Optional<CatalogComponentView> safeFormatMainboard(Mainboard mainboard) {
        try {
            return Optional.of(componentFormatter.format(mainboard, priceService.getLowestPrice(mainboard.getId()), priceService.getCheapestSourceName(mainboard.getId())));
        } catch (ResourceNotFoundException ex) {
            return Optional.empty();
        }
    }

    private Optional<CatalogComponentView> safeFormatRam(Ram ram) {
        try {
            return Optional.of(componentFormatter.format(ram, priceService.getLowestPrice(ram.getId()), priceService.getCheapestSourceName(ram.getId())));
        } catch (ResourceNotFoundException ex) {
            return Optional.empty();
        }
    }

    private Optional<CatalogComponentView> safeFormatGpu(Gpu gpu) {
        try {
            return Optional.of(componentFormatter.format(gpu, priceService.getLowestPrice(gpu.getId()), priceService.getCheapestSourceName(gpu.getId())));
        } catch (ResourceNotFoundException ex) {
            return Optional.empty();
        }
    }

    private Optional<CatalogComponentView> safeFormatPsu(Psu psu) {
        try {
            return Optional.of(componentFormatter.format(psu, priceService.getLowestPrice(psu.getId()), priceService.getCheapestSourceName(psu.getId())));
        } catch (ResourceNotFoundException ex) {
            return Optional.empty();
        }
    }

    private int score(CatalogComponentView item, String keyword) {
        int score = 0;
        if (item.name() != null && item.name().toLowerCase(Locale.ROOT).contains(keyword)) score += 100;
        if (item.brand() != null && item.brand().toLowerCase(Locale.ROOT).contains(keyword)) score += 60;
        if (item.type() != null && item.type().toLowerCase(Locale.ROOT).contains(keyword)) score += 30;
        if (item.specificationSummary() != null && item.specificationSummary().toLowerCase(Locale.ROOT).contains(keyword)) score += 20;
        return score;
    }

    private record SearchScore(CatalogComponentView view, int value) {
    }
}