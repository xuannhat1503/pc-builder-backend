package com.backend.service;

import com.backend.dto.ai.RecommendationResponse;

import java.math.BigDecimal;

public interface BuildRecommendationService {

    RecommendationResponse recommendByBudget(BigDecimal budget, String question);

    RecommendationResponse recommendByPurpose(String purpose, BigDecimal budget, String question);

    RecommendationResponse recommendByBrand(String brand, BigDecimal budget, String question);

    RecommendationResponse recommendCheaperAlternatives(String componentQuery);

    RecommendationResponse recommendBetterUpgrades(String componentQuery);
}