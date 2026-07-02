package com.backend.service;

import com.backend.dto.compatibility.CompatibilityReport;

import java.math.BigDecimal;
import java.util.List;

public interface CompatibilityService {

    CompatibilityReport checkCompatibility(List<Long> componentIds, BigDecimal totalPrice);
}