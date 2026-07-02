package com.backend.service.impl;

import com.backend.entity.BaseComponent;
import com.backend.entity.Cpu;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BaseComponentRepository;
import com.backend.repository.CpuRepository;
import com.backend.service.CpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CpuServiceImpl implements CpuService {

    private final CpuRepository repository;
    private final BaseComponentRepository baseComponentRepository;

    @Override
    public List<Cpu> findAll() {
        return repository.findAll();
    }

    @Override
    public Cpu findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay CPU co id = " + id));
    }

    @Override
    public Cpu create(Cpu cpu) {
        normalizeBaseComponent(cpu);
        cpu.setId(null);
        return repository.save(cpu);
    }

    @Override
    public Cpu update(Long id, Cpu cpu) {
        normalizeBaseComponent(cpu);
        cpu.setId(id);
        return repository.save(cpu);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalizeBaseComponent(Cpu cpu) {
        if (cpu.getBaseComponent() != null && cpu.getBaseComponent().getId() != null) {
            BaseComponent baseComponent = baseComponentRepository.findById(cpu.getBaseComponent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay base component co id = " + cpu.getBaseComponent().getId()));
            cpu.setBaseComponent(baseComponent);
        }
    }
}