package com.backend.service;

import java.math.BigDecimal;

public interface PriceService {

    BigDecimal getLowestPrice(Long componentId);

    String getCheapestSourceName(Long componentId);
}