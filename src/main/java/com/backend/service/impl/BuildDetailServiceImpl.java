package com.backend.service.impl;

import com.backend.entity.BaseComponent;
import com.backend.entity.Build;
import com.backend.entity.BuildDetail;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BaseComponentRepository;
import com.backend.repository.BuildDetailRepository;
import com.backend.repository.BuildRepository;
import com.backend.service.BuildCompatibilityService;
import com.backend.service.BuildDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildDetailServiceImpl implements BuildDetailService {

    private final BuildDetailRepository repository;
    private final BuildRepository buildRepository;
    private final BaseComponentRepository baseComponentRepository;
    private final BuildCompatibilityService compatibilityService;

    @Override
    public List<BuildDetail> findAll() {
        return repository.findAll();
    }

    @Override
    public List<BuildDetail> findByBuildId(Long buildId) {
        return repository.findAllByBuild_Id(buildId);
    }

    @Override
    public BuildDetail findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay build detail co id = " + id));
    }

    @Override
    public BuildDetail create(BuildDetail detail) {
        normalizeRelations(detail);
        detail.setId(null);
        return repository.save(detail);
    }

    @Override
    public BuildDetail update(Long id, BuildDetail detail) {
        normalizeRelations(detail);
        detail.setId(id);
        return repository.save(detail);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalizeRelations(BuildDetail detail) {
        if (detail.getBuild() != null && detail.getBuild().getId() != null) {
            Build build = buildRepository.findById(detail.getBuild().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay build co id = " + detail.getBuild().getId()));
            detail.setBuild(build);
        }

        if (detail.getComponent() != null && detail.getComponent().getId() != null) {
            BaseComponent component = baseComponentRepository.findById(detail.getComponent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay component co id = " + detail.getComponent().getId()));
            detail.setComponent(component);
        }
    }
}