package com.backend.service;

import com.backend.entity.BuildDetail;

import java.util.List;

public interface BuildDetailService {

    List<BuildDetail> findAll();

    List<BuildDetail> findByBuildId(Long buildId);

    BuildDetail findById(Long id);

    BuildDetail create(BuildDetail detail);

    BuildDetail update(Long id, BuildDetail detail);

    void delete(Long id);
}