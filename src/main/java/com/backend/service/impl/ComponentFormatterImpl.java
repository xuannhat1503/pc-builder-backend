package com.backend.service.impl;

import com.backend.dto.ai.CatalogComponentView;
import com.backend.entity.Cpu;
import com.backend.entity.Gpu;
import com.backend.entity.Mainboard;
import com.backend.entity.Psu;
import com.backend.entity.Ram;
import com.backend.service.ComponentFormatter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComponentFormatterImpl implements ComponentFormatter {

    @Override
    public CatalogComponentView format(Cpu cpu, BigDecimal lowestPrice, String cheapestSource) {
        return new CatalogComponentView(
                cpu.getId(),
                "CPU",
                cpu.getBaseComponent().getName(),
                cpu.getBaseComponent().getBrand(),
                cpu.getSocketType(),
                null,
                cpu.getTdpWattage(),
                null,
                null,
                null,
                "Socket %s, TDP %sW".formatted(cpu.getSocketType(), cpu.getTdpWattage()),
                lowestPrice,
                cheapestSource
        );
    }

    @Override
    public CatalogComponentView format(Mainboard mainboard, BigDecimal lowestPrice, String cheapestSource) {
        return new CatalogComponentView(
                mainboard.getId(),
                "MAINBOARD",
                mainboard.getBaseComponent().getName(),
                mainboard.getBaseComponent().getBrand(),
                mainboard.getSocketType(),
                mainboard.getRamGeneration(),
                null,
                null,
                null,
                null,
                "Socket %s, RAM %s".formatted(mainboard.getSocketType(), mainboard.getRamGeneration()),
                lowestPrice,
                cheapestSource
        );
    }

    @Override
    public CatalogComponentView format(Ram ram, BigDecimal lowestPrice, String cheapestSource) {
        return new CatalogComponentView(
                ram.getId(),
                "RAM",
                ram.getBaseComponent().getName(),
                ram.getBaseComponent().getBrand(),
                null,
                ram.getRamGeneration(),
                null,
                null,
                ram.getCapacityGb(),
                null,
                "RAM %s, Capacity %sGB".formatted(ram.getRamGeneration(), ram.getCapacityGb()),
                lowestPrice,
                cheapestSource
        );
    }

    @Override
    public CatalogComponentView format(Gpu gpu, BigDecimal lowestPrice, String cheapestSource) {
        return new CatalogComponentView(
                gpu.getId(),
                "GPU",
                gpu.getBaseComponent().getName(),
                gpu.getBaseComponent().getBrand(),
                null,
                null,
                gpu.getTdpWattage(),
                gpu.getVramSizeGb(),
                null,
                null,
                "VRAM %sGB, TDP %sW".formatted(gpu.getVramSizeGb(), gpu.getTdpWattage()),
                lowestPrice,
                cheapestSource
        );
    }

    @Override
    public CatalogComponentView format(Psu psu, BigDecimal lowestPrice, String cheapestSource) {
        return new CatalogComponentView(
                psu.getId(),
                "PSU",
                psu.getBaseComponent().getName(),
                psu.getBaseComponent().getBrand(),
                null,
                null,
                null,
                null,
                null,
                psu.getPowerOutputWatt(),
                "Power %sW".formatted(psu.getPowerOutputWatt()),
                lowestPrice,
                cheapestSource
        );
    }

    @Override
    public String formatCatalog(List<CatalogComponentView> components) {
        if (components == null || components.isEmpty()) {
            return "No catalog data available.";
        }

        Map<String, List<CatalogComponentView>> grouped = components.stream()
                .collect(Collectors.groupingBy(CatalogComponentView::type));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ":\n" + entry.getValue().stream()
                        .sorted(Comparator.comparing(CatalogComponentView::lowestPrice))
                        .map(item -> "- " + item.promptLine())
                        .collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n\n"));
    }
}