package com.backend.service;

import com.backend.dto.ai.ComparisonResponse;

public interface ComparisonService {

    ComparisonResponse compare(String leftQuery, String rightQuery);
}