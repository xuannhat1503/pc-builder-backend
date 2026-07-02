package com.backend.service;

import com.backend.entity.Build;

import java.util.List;

public interface BuildService {

    List<Build> findAll();

    List<Build> findByUserId(Long userId);

    Build findById(Long id);

    Build create(Build build);

    Build update(Long id, Build build);

    void delete(Long id);
}