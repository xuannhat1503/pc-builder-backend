package com.backend.service;

import java.math.BigDecimal;
import java.util.Optional;

public interface BudgetParser {

    Optional<BigDecimal> parseBudget(String message);
}