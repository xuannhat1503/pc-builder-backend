package com.backend.controller;

import com.backend.dto.mainboard.MainboardRequest;
import com.backend.entity.Mainboard;
import com.backend.service.MainboardService;
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
@RequestMapping("/api/mainboards")
public class MainboardController {

    private final MainboardService service;

    @GetMapping
    public List<Mainboard> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mainboard> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Mainboard> create(@Valid @RequestBody MainboardRequest request) {
        Mainboard mainboard = new Mainboard();
        mainboard.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        mainboard.setSocketType(request.socketType());
        mainboard.setRamGeneration(request.ramGeneration());
        return ResponseEntity.ok(service.create(mainboard));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mainboard> update(@PathVariable Long id, @Valid @RequestBody MainboardRequest request) {
        Mainboard mainboard = new Mainboard();
        mainboard.setId(id);
        mainboard.setBaseComponent(new com.backend.entity.BaseComponent(request.baseComponentId(), null, null));
        mainboard.setSocketType(request.socketType());
        mainboard.setRamGeneration(request.ramGeneration());
        return ResponseEntity.ok(service.update(id, mainboard));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}