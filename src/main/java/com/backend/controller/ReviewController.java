package com.backend.controller;

import com.backend.dto.review.ReviewRequest;
import com.backend.entity.BaseComponent;
import com.backend.entity.Review;
import com.backend.entity.User;
import com.backend.service.ReviewService;
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
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService service;

    @GetMapping
    public List<Review> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/component/{componentId}")
    public List<Review> findByComponent(@PathVariable Long componentId) {
        return service.findByComponentId(componentId);
    }

    @PostMapping
    public ResponseEntity<Review> create(@Valid @RequestBody ReviewRequest request) {
        Review review = new Review();
        review.setUser(new User(request.userId(), null, null, null));
        review.setComponent(new BaseComponent(request.componentId(), null, null));
        review.setRatingStar(request.ratingStar());
        review.setCommentText(request.commentText());
        return ResponseEntity.ok(service.create(review));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> update(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        Review review = new Review();
        review.setId(id);
        review.setUser(new User(request.userId(), null, null, null));
        review.setComponent(new BaseComponent(request.componentId(), null, null));
        review.setRatingStar(request.ratingStar());
        review.setCommentText(request.commentText());
        return ResponseEntity.ok(service.update(id, review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}