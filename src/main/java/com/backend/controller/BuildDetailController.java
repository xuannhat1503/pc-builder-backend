package com.backend.controller;

import com.backend.dto.builddetail.BuildDetailRequest;
import com.backend.entity.BaseComponent;
import com.backend.entity.BuildDetail;
import com.backend.service.BuildDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/build-details")
public class BuildDetailController {

    private final BuildDetailService service;

    @GetMapping
    public List<BuildDetail> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildDetail> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/build/{buildId}")
    public List<BuildDetail> findByBuild(@PathVariable Long buildId) {
        return service.findByBuildId(buildId);
    }

    @PostMapping
    public ResponseEntity<BuildDetail> create(@Valid @RequestBody BuildDetailRequest request) {
        BuildDetail detail = new BuildDetail();
        com.backend.entity.Build build = new com.backend.entity.Build();
        build.setId(request.buildId());
        detail.setBuild(build);
        detail.setComponent(new BaseComponent(request.componentId(), null, null));
        detail.setQuantity(request.quantity());
        return ResponseEntity.ok(service.create(detail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuildDetail> update(@PathVariable Long id, @Valid @RequestBody BuildDetailRequest request) {
        BuildDetail detail = new BuildDetail();
        detail.setId(id);
        com.backend.entity.Build build = new com.backend.entity.Build();
        build.setId(request.buildId());
        detail.setBuild(build);
        detail.setComponent(new BaseComponent(request.componentId(), null, null));
        detail.setQuantity(request.quantity());
        return ResponseEntity.ok(service.update(id, detail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}