package com.backend.controller;

import com.backend.dto.compatibility.CompatibilityCheckRequest;
import com.backend.dto.compatibility.CompatibilityReport;
import com.backend.service.BuildCompatibilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/compatibility")
public class CompatibilityController {

    private final BuildCompatibilityService service;

    @PostMapping("/check")
    public ResponseEntity<CompatibilityReport> check(@Valid @RequestBody CompatibilityCheckRequest request) {
        return ResponseEntity.ok(service.checkCompatibility(request));
    }
}
