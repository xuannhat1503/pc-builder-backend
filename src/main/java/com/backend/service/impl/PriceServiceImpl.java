package com.backend.service.impl;

import com.backend.entity.CrawledPrice;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.CrawledPriceRepository;
import com.backend.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final CrawledPriceRepository repository;

    @Override
    public BigDecimal getLowestPrice(Long componentId) {
        return findLowest(componentId).priceValue();
    }

    @Override
    public String getCheapestSourceName(Long componentId) {
        return findLowest(componentId).sourceName();
    }

    private PriceSnapshot findLowest(Long componentId) {
        List<CrawledPrice> prices = repository.findAllByComponent_Id(componentId);
        if (prices.isEmpty()) {
            throw new ResourceNotFoundException("Khong tim thay gia crawl cho component co id = " + componentId);
        }

        CrawledPrice cheapest = prices.stream()
                .min(Comparator.comparing(CrawledPrice::getPriceValue))
                .orElseThrow();

        return new PriceSnapshot(cheapest.getPriceValue(), cheapest.getSourceName());
    }

    private record PriceSnapshot(BigDecimal priceValue, String sourceName) {
    }
}