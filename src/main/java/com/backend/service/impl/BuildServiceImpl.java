package com.backend.service.impl;

import com.backend.entity.Build;
// approval enum removed
import com.backend.entity.User;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BuildRepository;
import com.backend.repository.UserRepository;
import com.backend.service.BuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildServiceImpl implements BuildService {

    private final BuildRepository repository;
    private final UserRepository userRepository;

    @Override
    public List<Build> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Build> findByUserId(Long userId) {
        return repository.findAllByUser_Id(userId);
    }

    @Override
    public Build findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay build co id = " + id));
    }

    @Override
    public Build create(Build build) {
        normalizeUser(build);
        build.setId(null);
        return repository.save(build);
    }

    @Override
    public Build update(Long id, Build build) {
        normalizeUser(build);
        build.setId(id);
        return repository.save(build);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalizeUser(Build build) {
        if (build.getUser() != null && build.getUser().getId() != null) {
            User user = userRepository.findById(build.getUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user co id = " + build.getUser().getId()));
            build.setUser(user);
        }
    }
}