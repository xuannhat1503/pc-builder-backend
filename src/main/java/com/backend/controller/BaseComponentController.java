package com.backend.controller;

import com.backend.dto.basecomponent.BaseComponentRequest;
import com.backend.entity.BaseComponent;
import com.backend.service.BaseComponentService;
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
@RequestMapping("/api/base-components")
public class BaseComponentController {

    private final BaseComponentService service;

    @GetMapping
    public List<BaseComponent> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseComponent> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<BaseComponent> create(@Valid @RequestBody BaseComponentRequest request) {
        return ResponseEntity.ok(service.create(new BaseComponent(null, request.name(), request.brand())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseComponent> update(@PathVariable Long id, @Valid @RequestBody BaseComponentRequest request) {
        return ResponseEntity.ok(service.update(id, new BaseComponent(id, request.name(), request.brand())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}