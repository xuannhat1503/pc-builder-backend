package com.backend.service.impl;

import com.backend.entity.BaseComponent;
import com.backend.entity.Ram;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BaseComponentRepository;
import com.backend.repository.RamRepository;
import com.backend.service.RamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RamServiceImpl implements RamService {

    private final RamRepository repository;
    private final BaseComponentRepository baseComponentRepository;

    @Override
    public List<Ram> findAll() {
        return repository.findAll();
    }

    @Override
    public Ram findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay RAM co id = " + id));
    }

    @Override
    public Ram create(Ram ram) {
        normalizeBaseComponent(ram);
        ram.setId(null);
        return repository.save(ram);
    }

    @Override
    public Ram update(Long id, Ram ram) {
        normalizeBaseComponent(ram);
        ram.setId(id);
        return repository.save(ram);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalizeBaseComponent(Ram ram) {
        if (ram.getBaseComponent() != null && ram.getBaseComponent().getId() != null) {
            BaseComponent baseComponent = baseComponentRepository.findById(ram.getBaseComponent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay base component co id = " + ram.getBaseComponent().getId()));
            ram.setBaseComponent(baseComponent);
        }
    }
}