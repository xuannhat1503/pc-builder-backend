package com.backend.controller;

import com.backend.dto.ram.RamRequest;
import com.backend.entity.Ram;
import com.backend.service.RamService;
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
@RequestMapping("/api/rams")
public class RamController {

    private final RamService service;

    @GetMapping
    public List<Ram> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ram> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Ram> create(@Valid @RequestBody RamRequest request) {
        Ram ram = new Ram();
        ram.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        ram.setRamGeneration(request.ramGeneration());
        ram.setCapacityGb(request.capacityGb());
        return ResponseEntity.ok(service.create(ram));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ram> update(@PathVariable Long id, @Valid @RequestBody RamRequest request) {
        Ram ram = new Ram();
        ram.setId(id);
        ram.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        ram.setRamGeneration(request.ramGeneration());
        ram.setCapacityGb(request.capacityGb());
        return ResponseEntity.ok(service.update(id, ram));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}