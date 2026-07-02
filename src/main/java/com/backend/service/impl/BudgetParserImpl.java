package com.backend.service.impl;

import com.backend.service.BudgetParser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BudgetParserImpl implements BudgetParser {

    private static final Pattern BUDGET_PATTERN = Pattern.compile(
            "(?i)(?:budget|ngan sach|ngân sách|under|duoi|dưới)?\\s*([0-9]+(?:[.,][0-9]+)?)\\s*(trieu|tr|m|million|đ|d|vnd)?"
    );

    @Override
    public Optional<BigDecimal> parseBudget(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = BUDGET_PATTERN.matcher(message);
        while (matcher.find()) {
            String rawNumber = matcher.group(1);
            String unit = matcher.group(2) == null ? "" : matcher.group(2).toLowerCase();
            BigDecimal amount = normalizeNumber(rawNumber);
            if (amount == null) {
                continue;
            }

            if (unit.contains("tr") || unit.contains("m") || unit.contains("million") || unit.contains("trieu") || unit.contains("triệu")) {
                return Optional.of(amount.multiply(BigDecimal.valueOf(1_000_000L)));
            }

            if (unit.contains("đ") || unit.contains("d") || unit.contains("vnd")) {
                return Optional.of(amount);
            }

            if (amount.compareTo(BigDecimal.valueOf(1000)) < 0) {
                return Optional.of(amount.multiply(BigDecimal.valueOf(1_000_000L)));
            }

            return Optional.of(amount);
        }

        return Optional.empty();
    }

    private BigDecimal normalizeNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String cleaned = raw.trim().replace(" ", "");
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else if (cleaned.contains(",")) {
            cleaned = cleaned.replace(",", ".");
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}