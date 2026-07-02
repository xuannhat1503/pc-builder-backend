package com.backend.service;

import com.backend.dto.compatibility.CompatibilityCheckRequest;
import com.backend.dto.compatibility.CompatibilityReport;

public interface BuildCompatibilityService {

    CompatibilityReport checkCompatibility(CompatibilityCheckRequest request);
}