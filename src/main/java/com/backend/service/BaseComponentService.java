package com.backend.service;

import com.backend.entity.BaseComponent;

import java.util.List;

public interface BaseComponentService {

    List<BaseComponent> findAll();

    BaseComponent findById(Long id);

    BaseComponent create(BaseComponent component);

    BaseComponent update(Long id, BaseComponent component);

    void delete(Long id);
}