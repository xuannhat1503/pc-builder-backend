package com.backend.service;

import com.backend.entity.Review;

import java.util.List;

public interface ReviewService {

    List<Review> findAll();

    List<Review> findByComponentId(Long componentId);

    Review findById(Long id);

    Review create(Review review);

    Review update(Long id, Review review);

    void delete(Long id);
}