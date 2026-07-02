package com.backend.controller;

import com.backend.entity.BaseComponent;
import com.backend.entity.CrawledPrice;
import com.backend.dto.price.CrawledPriceRequest;
import com.backend.dto.price.CrawledPriceCrawlRequest;
import com.backend.dto.price.PriceSummaryReport;
import com.backend.service.CrawledPriceService;
import com.backend.service.PriceInsightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawled-prices")
public class CrawledPriceController {

    private final CrawledPriceService service;
    private final PriceInsightService priceInsightService;

    @GetMapping
    public List<CrawledPrice> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrawledPrice> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/component/{componentId}")
    public List<CrawledPrice> findByComponent(@PathVariable Long componentId) {
        return service.findByComponentId(componentId);
    }

    @GetMapping("/component/{componentId}/summary")
    public ResponseEntity<PriceSummaryReport> summarizeByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(priceInsightService.summarizeComponentPrice(componentId));
    }

    @GetMapping("/sources")
    public List<String> sourceNames() {
        return service.findDistinctSourceNames();
    }

    @PostMapping
    public ResponseEntity<CrawledPrice> create(@Valid @RequestBody CrawledPriceRequest request) {
        CrawledPrice crawledPrice = new CrawledPrice();
        crawledPrice.setComponent(new BaseComponent(request.componentId(), null, null));
        crawledPrice.setSourceName(request.sourceName());
        crawledPrice.setPriceValue(request.priceValue());
        return ResponseEntity.ok(service.create(crawledPrice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrawledPrice> update(@PathVariable Long id, @Valid @RequestBody CrawledPriceRequest request) {
        CrawledPrice crawledPrice = new CrawledPrice();
        crawledPrice.setId(id);
        crawledPrice.setComponent(new BaseComponent(request.componentId(), null, null));
        crawledPrice.setSourceName(request.sourceName());
        crawledPrice.setPriceValue(request.priceValue());
        return ResponseEntity.ok(service.update(id, crawledPrice));
    }

    @PostMapping("/crawl")
    public ResponseEntity<CrawledPrice> crawl(@Valid @RequestBody CrawledPriceCrawlRequest request) {
        return ResponseEntity.ok(service.crawlAndSave(request.componentId(), request.sourceUrl(), request.sourceName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}