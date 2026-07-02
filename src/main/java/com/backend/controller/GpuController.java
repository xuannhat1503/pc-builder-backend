package com.backend.controller;

import com.backend.dto.gpu.GpuRequest;
import com.backend.entity.Gpu;
import com.backend.service.GpuService;
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
@RequestMapping("/api/gpus")
public class GpuController {

    private final GpuService service;

    @GetMapping
    public List<Gpu> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Gpu> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Gpu> create(@Valid @RequestBody GpuRequest request) {
        Gpu gpu = new Gpu();
        gpu.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        gpu.setVramSizeGb(request.vramSizeGb());
        gpu.setTdpWattage(request.tdpWattage());
        return ResponseEntity.ok(service.create(gpu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gpu> update(@PathVariable Long id, @Valid @RequestBody GpuRequest request) {
        Gpu gpu = new Gpu();
        gpu.setId(id);
        gpu.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        gpu.setVramSizeGb(request.vramSizeGb());
        gpu.setTdpWattage(request.tdpWattage());
        return ResponseEntity.ok(service.update(id, gpu));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}