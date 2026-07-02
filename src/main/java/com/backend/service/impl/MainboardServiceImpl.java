package com.backend.service.impl;

import com.backend.entity.BaseComponent;
import com.backend.entity.Mainboard;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BaseComponentRepository;
import com.backend.repository.MainboardRepository;
import com.backend.service.MainboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainboardServiceImpl implements MainboardService {

    private final MainboardRepository repository;
    private final BaseComponentRepository baseComponentRepository;

    @Override
    public List<Mainboard> findAll() {
        return repository.findAll();
    }

    @Override
    public Mainboard findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mainboard co id = " + id));
    }

    @Override
    public Mainboard create(Mainboard mainboard) {
        normalizeBaseComponent(mainboard);
        mainboard.setId(null);
        return repository.save(mainboard);
    }

    @Override
    public Mainboard update(Long id, Mainboard mainboard) {
        normalizeBaseComponent(mainboard);
        mainboard.setId(id);
        return repository.save(mainboard);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalizeBaseComponent(Mainboard mainboard) {
        if (mainboard.getBaseComponent() != null && mainboard.getBaseComponent().getId() != null) {
            BaseComponent baseComponent = baseComponentRepository.findById(mainboard.getBaseComponent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay base component co id = " + mainboard.getBaseComponent().getId()));
            mainboard.setBaseComponent(baseComponent);
        }
    }
}