package com.backend.service;

import com.backend.dto.price.PriceSummaryReport;

public interface PriceInsightService {

    PriceSummaryReport summarizeComponentPrice(Long componentId);
}