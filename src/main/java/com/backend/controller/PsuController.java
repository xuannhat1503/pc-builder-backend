package com.backend.controller;

import com.backend.dto.psu.PsuRequest;
import com.backend.entity.Psu;
import com.backend.service.PsuService;
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
@RequestMapping("/api/psus")
public class PsuController {

    private final PsuService service;

    @GetMapping
    public List<Psu> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Psu> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Psu> create(@Valid @RequestBody PsuRequest request) {
        Psu psu = new Psu();
        psu.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        psu.setPowerOutputWatt(request.powerOutputWatt());
        return ResponseEntity.ok(service.create(psu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Psu> update(@PathVariable Long id, @Valid @RequestBody PsuRequest request) {
        Psu psu = new Psu();
        psu.setId(id);
        psu.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        psu.setPowerOutputWatt(request.powerOutputWatt());
        return ResponseEntity.ok(service.update(id, psu));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}