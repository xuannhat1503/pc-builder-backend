package com.backend.service;

import com.backend.entity.Psu;

import java.util.List;

public interface PsuService {

    List<Psu> findAll();

    Psu findById(Long id);

    Psu create(Psu psu);

    Psu update(Long id, Psu psu);

    void delete(Long id);
}