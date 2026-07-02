package com.backend.service;

import com.backend.entity.Ram;

import java.util.List;

public interface RamService {

    List<Ram> findAll();

    Ram findById(Long id);

    Ram create(Ram ram);

    Ram update(Long id, Ram ram);

    void delete(Long id);
}