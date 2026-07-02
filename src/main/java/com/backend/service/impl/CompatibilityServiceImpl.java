package com.backend.service.impl;

import com.backend.dto.compatibility.CompatibilityCheckComponent;
import com.backend.dto.compatibility.CompatibilityCheckRequest;
import com.backend.dto.compatibility.CompatibilityReport;
import com.backend.service.BuildCompatibilityService;
import com.backend.service.CompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompatibilityServiceImpl implements CompatibilityService {

    private final BuildCompatibilityService buildCompatibilityService;

    @Override
    public CompatibilityReport checkCompatibility(List<Long> componentIds, BigDecimal totalPrice) {
        List<CompatibilityCheckComponent> components = componentIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .map(id -> new CompatibilityCheckComponent(id, 1))
                .toList();

        return buildCompatibilityService.checkCompatibility(new CompatibilityCheckRequest(totalPrice, components));
    }
}