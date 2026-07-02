package com.backend.service.impl;

import com.backend.entity.BaseComponent;
import com.backend.entity.Psu;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BaseComponentRepository;
import com.backend.repository.PsuRepository;
import com.backend.service.PsuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PsuServiceImpl implements PsuService {

    private final PsuRepository repository;
    private final BaseComponentRepository baseComponentRepository;

    @Override
    public List<Psu> findAll() {
        return repository.findAll();
    }

    @Override
    public Psu findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay PSU co id = " + id));
    }

    @Override
    public Psu create(Psu psu) {
        normalizeBaseComponent(psu);
        psu.setId(null);
        return repository.save(psu);
    }

    @Override
    public Psu update(Long id, Psu psu) {
        normalizeBaseComponent(psu);
        psu.setId(id);
        return repository.save(psu);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalizeBaseComponent(Psu psu) {
        if (psu.getBaseComponent() != null && psu.getBaseComponent().getId() != null) {
            BaseComponent baseComponent = baseComponentRepository.findById(psu.getBaseComponent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay base component co id = " + psu.getBaseComponent().getId()));
            psu.setBaseComponent(baseComponent);
        }
    }
}