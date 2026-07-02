package com.backend.controller;

import com.backend.dto.build.BuildRequest;
import com.backend.entity.Build;
import com.backend.entity.User;
import com.backend.service.BuildService;
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
@RequestMapping("/api/builds")
public class BuildController {

    private final BuildService service;

    @GetMapping
    public List<Build> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Build> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/user/{userId}")
    public List<Build> findByUser(@PathVariable Long userId) {
        return service.findByUserId(userId);
    }

    @GetMapping("/public")
    public List<Build> findPublicBuilds() {
        // Public builds no longer require approval; return all builds instead
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<Build> create(@Valid @RequestBody BuildRequest request) {
        Build build = new Build();
        build.setUser(new User(request.userId(), null, null, null));
        build.setTitle(request.title());
        build.setDescription(request.description());
        build.setTotalPrice(request.totalPrice());
        build.setCompatible(request.compatible() != null ? request.compatible() : Boolean.TRUE);
        return ResponseEntity.ok(service.create(build));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Build> update(@PathVariable Long id, @Valid @RequestBody BuildRequest request) {
        Build build = new Build();
        build.setId(id);
        build.setUser(new User(request.userId(), null, null, null));
        build.setTitle(request.title());
        build.setDescription(request.description());
        build.setTotalPrice(request.totalPrice());
        build.setCompatible(request.compatible() != null ? request.compatible() : Boolean.TRUE);
        return ResponseEntity.ok(service.update(id, build));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}