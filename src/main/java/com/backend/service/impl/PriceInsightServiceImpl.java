package com.backend.service.impl;

import com.backend.dto.price.PriceSummaryReport;
import com.backend.entity.CrawledPrice;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.CrawledPriceRepository;
import com.backend.service.PriceInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceInsightServiceImpl implements PriceInsightService {

    private final CrawledPriceRepository repository;

    @Override
    public PriceSummaryReport summarizeComponentPrice(Long componentId) {
        List<CrawledPrice> prices = repository.findAllByComponent_Id(componentId);
        if (prices.isEmpty()) {
            throw new ResourceNotFoundException("Khong tim thay gia crawl cho component co id = " + componentId);
        }

        BigDecimal minPrice = prices.stream()
                .map(CrawledPrice::getPriceValue)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = prices.stream()
                .map(CrawledPrice::getPriceValue)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal averagePrice = prices.stream()
                .map(CrawledPrice::getPriceValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);

        CrawledPrice cheapest = prices.stream()
                .min(Comparator.comparing(CrawledPrice::getPriceValue))
                .orElseThrow();

        List<PriceSummaryReport.PricePoint> points = prices.stream()
                .sorted(Comparator.comparing(CrawledPrice::getPriceValue))
                .map(price -> new PriceSummaryReport.PricePoint(
                        price.getId(),
                        price.getSourceName(),
                        price.getPriceValue()
                ))
                .toList();

        return new PriceSummaryReport(
                componentId,
                prices.size(),
                minPrice,
                maxPrice,
                averagePrice,
                cheapest.getSourceName(),
                points
        );
    }
}