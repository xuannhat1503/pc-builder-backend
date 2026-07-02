package com.backend.service.impl;

import com.backend.entity.BaseComponent;
import com.backend.entity.Review;
import com.backend.entity.User;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BaseComponentRepository;
import com.backend.repository.ReviewRepository;
import com.backend.repository.UserRepository;
import com.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;
    private final UserRepository userRepository;
    private final BaseComponentRepository baseComponentRepository;

    @Override
    public List<Review> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Review> findByComponentId(Long componentId) {
        return repository.findAllByComponent_Id(componentId);
    }

    @Override
    public Review findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay review co id = " + id));
    }

    @Override
    public Review create(Review review) {
        normalizeRelations(review);
        review.setId(null);
        return repository.save(review);
    }

    @Override
    public Review update(Long id, Review review) {
        normalizeRelations(review);
        review.setId(id);
        return repository.save(review);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalizeRelations(Review review) {
        if (review.getUser() != null && review.getUser().getId() != null) {
            User user = userRepository.findById(review.getUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user co id = " + review.getUser().getId()));
            review.setUser(user);
        }

        if (review.getComponent() != null && review.getComponent().getId() != null) {
            BaseComponent component = baseComponentRepository.findById(review.getComponent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay component co id = " + review.getComponent().getId()));
            review.setComponent(component);
        }
    }
}