package com.backend.service;

import com.backend.entity.CrawledPrice;

import java.util.List;

public interface CrawledPriceService {

    List<CrawledPrice> findAll();

    List<CrawledPrice> findByComponentId(Long componentId);

    List<String> findDistinctSourceNames();

    CrawledPrice findById(Long id);

    CrawledPrice create(CrawledPrice crawledPrice);

    CrawledPrice update(Long id, CrawledPrice crawledPrice);

    CrawledPrice crawlAndSave(Long componentId, String sourceUrl, String sourceName);

    void delete(Long id);
}