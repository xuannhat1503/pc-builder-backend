package com.backend.service;

import com.backend.entity.Mainboard;

import java.util.List;

public interface MainboardService {

    List<Mainboard> findAll();

    Mainboard findById(Long id);

    Mainboard create(Mainboard mainboard);

    Mainboard update(Long id, Mainboard mainboard);

    void delete(Long id);
}