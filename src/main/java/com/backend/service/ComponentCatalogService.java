package com.backend.service;

import com.backend.dto.ai.CatalogComponentView;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ComponentCatalogService {

    List<CatalogComponentView> findAll();

    Map<String, List<CatalogComponentView>> groupByType();

    List<CatalogComponentView> findByBrand(String brand);

    List<CatalogComponentView> search(String query, int limit);

    Optional<CatalogComponentView> findBestMatch(String query);
}