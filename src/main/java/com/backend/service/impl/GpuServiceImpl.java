package com.backend.service.impl;

import com.backend.entity.BaseComponent;
import com.backend.entity.Gpu;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BaseComponentRepository;
import com.backend.repository.GpuRepository;
import com.backend.service.GpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GpuServiceImpl implements GpuService {

    private final GpuRepository repository;
    private final BaseComponentRepository baseComponentRepository;

    @Override
    public List<Gpu> findAll() {
        return repository.findAll();
    }

    @Override
    public Gpu findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay GPU co id = " + id));
    }

    @Override
    public Gpu create(Gpu gpu) {
        normalizeBaseComponent(gpu);
        gpu.setId(null);
        return repository.save(gpu);
    }

    @Override
    public Gpu update(Long id, Gpu gpu) {
        normalizeBaseComponent(gpu);
        gpu.setId(id);
        return repository.save(gpu);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalizeBaseComponent(Gpu gpu) {
        if (gpu.getBaseComponent() != null && gpu.getBaseComponent().getId() != null) {
            BaseComponent baseComponent = baseComponentRepository.findById(gpu.getBaseComponent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay base component co id = " + gpu.getBaseComponent().getId()));
            gpu.setBaseComponent(baseComponent);
        }
    }
}