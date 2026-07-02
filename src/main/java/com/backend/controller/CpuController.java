package com.backend.controller;

import com.backend.dto.cpu.CpuRequest;
import com.backend.entity.Cpu;
import com.backend.service.CpuService;
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
@RequestMapping("/api/cpus")
public class CpuController {

    private final CpuService service;

    @GetMapping
    public List<Cpu> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cpu> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Cpu> create(@Valid @RequestBody CpuRequest request) {
        Cpu cpu = new Cpu();
        cpu.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        cpu.setSocketType(request.socketType());
        cpu.setTdpWattage(request.tdpWattage());
        return ResponseEntity.ok(service.create(cpu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cpu> update(@PathVariable Long id, @Valid @RequestBody CpuRequest request) {
        Cpu cpu = new Cpu();
        cpu.setId(id);
        cpu.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        cpu.setSocketType(request.socketType());
        cpu.setTdpWattage(request.tdpWattage());
        return ResponseEntity.ok(service.update(id, cpu));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}