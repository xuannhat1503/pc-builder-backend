package com.backend.service;

import com.backend.entity.Cpu;

import java.util.List;

public interface CpuService {

    List<Cpu> findAll();

    Cpu findById(Long id);

    Cpu create(Cpu cpu);

    Cpu update(Long id, Cpu cpu);

    void delete(Long id);
}