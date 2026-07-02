package com.backend.service.impl;

import com.backend.dto.compatibility.CompatibilityCheckComponent;
import com.backend.dto.compatibility.CompatibilityCheckRequest;
import com.backend.dto.compatibility.CompatibilityIssue;
import com.backend.dto.compatibility.CompatibilityReport;
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
import com.backend.service.BuildCompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BuildCompatibilityServiceImpl implements BuildCompatibilityService {

    private static final int POWER_HEADROOM_WATT = 120;

    private final CpuRepository cpuRepository;
    private final MainboardRepository mainboardRepository;
    private final RamRepository ramRepository;
    private final PsuRepository psuRepository;
    private final GpuRepository gpuRepository;

    @Override
    public CompatibilityReport checkCompatibility(CompatibilityCheckRequest request) {
        Map<Long, Integer> quantities = quantityMap(request.components());
        List<Long> componentIds = new ArrayList<>(quantities.keySet());

        Cpu cpu = findCpu(componentIds);
        Mainboard mainboard = findMainboard(componentIds);
        Ram ram = findRam(componentIds);
        Psu psu = findPsu(componentIds);
        List<Gpu> gpus = findGpus(componentIds);

        List<CompatibilityIssue> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (request.components().size() < 2) {
            issues.add(new CompatibilityIssue("NOT_ENOUGH_COMPONENTS", "warning", "Nen gui toi thieu 2 linh kien de check tuong thich."));
            suggestions.add("Them it nhat 2 linh kien, vi du CPU va mainboard, de check tuong thich.");
        }

        if (cpu == null) {
            issues.add(new CompatibilityIssue("CPU_MISSING", "warning", "Build chua co CPU."));
            suggestions.add("Them CPU de hoan chinh cau hinh.");
        }

        if (mainboard == null) {
            issues.add(new CompatibilityIssue("MAINBOARD_MISSING", "warning", "Build chua co mainboard."));
            suggestions.add("Them mainboard cung socket voi CPU.");
        }

        if (ram == null) {
            issues.add(new CompatibilityIssue("RAM_MISSING", "warning", "Build chua co RAM."));
            suggestions.add("Them RAM co dung gen voi mainboard.");
        }

        if (psu == null) {
            issues.add(new CompatibilityIssue("PSU_MISSING", "warning", "Build chua co PSU."));
            suggestions.add("Them PSU du cong suat.");
        }

        if (cpu != null && mainboard != null && !safeEquals(cpu.getSocketType(), mainboard.getSocketType())) {
            issues.add(new CompatibilityIssue("SOCKET_MISMATCH", "error", "CPU socket khong khop voi mainboard."));
            suggestions.add("Chon mainboard dung socket voi CPU hoac doi CPU phu hop mainboard.");
        }

        if (ram != null && mainboard != null && !safeEquals(ram.getRamGeneration(), mainboard.getRamGeneration())) {
            issues.add(new CompatibilityIssue("RAM_GEN_MISMATCH", "error", "RAM generation khong khop voi mainboard."));
            suggestions.add("Dung RAM co cung generation voi mainboard.");
        }

        int estimatedPowerWatt = estimatePower(cpu, gpus, quantities);
        int recommendedPsuWatt = estimatedPowerWatt + POWER_HEADROOM_WATT;

        if (psu != null && psu.getPowerOutputWatt() != null && psu.getPowerOutputWatt() < recommendedPsuWatt) {
            issues.add(new CompatibilityIssue("PSU_INSUFFICIENT", "error", "Cong suat PSU chua du an toan."));
            suggestions.add("Nang cap PSU len toi thieu " + recommendedPsuWatt + "W de co headroom an toan.");
        }

        if (issues.isEmpty()) {
            suggestions.add("Cau hinh hien tai dang tuong thich co ban.");
        }

        boolean compatible = issues.stream().noneMatch(issue -> "error".equalsIgnoreCase(issue.severity()));

        return new CompatibilityReport(
                null,
                compatible,
                cpu != null ? cpu.getSocketType() : null,
                mainboard != null ? mainboard.getSocketType() : null,
                ram != null ? ram.getRamGeneration() : null,
                estimatedPowerWatt,
                recommendedPsuWatt,
                request.totalPrice(),
                issues,
                suggestions
        );
    }

    private int estimatePower(Cpu cpu, List<Gpu> gpus, Map<Long, Integer> quantities) {
        int total = 0;

        if (cpu != null && cpu.getTdpWattage() != null) {
            total += cpu.getTdpWattage();
        }

        for (Gpu gpu : gpus) {
            if (gpu.getTdpWattage() != null) {
                int quantity = quantities.getOrDefault(gpu.getId(), 1);
                total += gpu.getTdpWattage() * Math.max(quantity, 1);
            }
        }

        return total;
    }

    private Map<Long, Integer> quantityMap(List<CompatibilityCheckComponent> components) {
        Map<Long, Integer> quantities = new HashMap<>();
        for (CompatibilityCheckComponent component : components) {
            if (component != null && component.componentId() != null) {
                quantities.merge(component.componentId(), component.quantity() == null ? 1 : component.quantity(), Integer::sum);
            }
        }
        return quantities;
    }

    private Cpu findCpu(List<Long> componentIds) {
        return cpuRepository.findAllById(componentIds).stream()
                .sorted(Comparator.comparing(Cpu::getId))
                .findFirst()
                .orElse(null);
    }

    private Mainboard findMainboard(List<Long> componentIds) {
        return mainboardRepository.findAllById(componentIds).stream()
                .sorted(Comparator.comparing(Mainboard::getId))
                .findFirst()
                .orElse(null);
    }

    private Ram findRam(List<Long> componentIds) {
        return ramRepository.findAllById(componentIds).stream()
                .sorted(Comparator.comparing(Ram::getId))
                .findFirst()
                .orElse(null);
    }

    private Psu findPsu(List<Long> componentIds) {
        return psuRepository.findAllById(componentIds).stream()
                .sorted(Comparator.comparing(Psu::getId))
                .findFirst()
                .orElse(null);
    }

    private List<Gpu> findGpus(List<Long> componentIds) {
        return gpuRepository.findAllById(componentIds).stream()
                .sorted(Comparator.comparing(Gpu::getId))
                .toList();
    }

    private boolean safeEquals(String left, String right) {
        return left == null ? right == null : left.equalsIgnoreCase(right);
    }
}