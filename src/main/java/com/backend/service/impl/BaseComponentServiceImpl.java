package com.backend.service.impl;

import com.backend.entity.BaseComponent;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BaseComponentRepository;
import com.backend.service.BaseComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseComponentServiceImpl implements BaseComponentService {

    private final BaseComponentRepository repository;

    @Override
    public List<BaseComponent> findAll() {
        return repository.findAll();
    }

    @Override
    public BaseComponent findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay base component co id = " + id));
    }

    @Override
    public BaseComponent create(BaseComponent component) {
        component.setId(null);
        return repository.save(component);
    }

    @Override
    public BaseComponent update(Long id, BaseComponent component) {
        component.setId(id);
        return repository.save(component);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}