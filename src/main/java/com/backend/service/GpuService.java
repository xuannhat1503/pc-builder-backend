package com.backend.service;

import com.backend.entity.Gpu;

import java.util.List;

public interface GpuService {

    List<Gpu> findAll();

    Gpu findById(Long id);

    Gpu create(Gpu gpu);

    Gpu update(Long id, Gpu gpu);

    void delete(Long id);
}