package com.backend.service.impl;

import com.backend.dto.ai.CatalogComponentView;
import com.backend.dto.ai.ComparisonResponse;
import com.backend.service.ComponentCatalogService;
import com.backend.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComparisonServiceImpl implements ComparisonService {

    private final ComponentCatalogService catalogService;

    @Override
    public ComparisonResponse compare(String leftQuery, String rightQuery) {
        Optional<CatalogComponentView> left = catalogService.findBestMatch(leftQuery);
        Optional<CatalogComponentView> right = catalogService.findBestMatch(rightQuery);

        if (left.isEmpty() || right.isEmpty()) {
            return new ComparisonResponse(null, null, null, "I don't have enough data.", List.of());
        }

        CatalogComponentView leftView = left.get();
        CatalogComponentView rightView = right.get();
        List<String> differences = new ArrayList<>();

        addDiff(differences, "Brand", leftView.brand(), rightView.brand());
        addDiff(differences, "Price", String.valueOf(leftView.lowestPrice()), String.valueOf(rightView.lowestPrice()));
        addDiff(differences, "Spec", leftView.specificationSummary(), rightView.specificationSummary());

        String verdict = compareVerdict(leftView, rightView);
        String reason = "So sánh dựa trên dữ liệu hiện có trong database.";

        return new ComparisonResponse(
                leftView.name(),
                rightView.name(),
                verdict,
                reason,
                List.copyOf(differences)
        );
    }

    private String compareVerdict(CatalogComponentView left, CatalogComponentView right) {
        if (sameIgnoreCase(left.type(), "GPU") || sameIgnoreCase(left.type(), "CPU")) {
            int leftMetric = metric(left);
            int rightMetric = metric(right);
            if (leftMetric > rightMetric) return left.name();
            if (rightMetric > leftMetric) return right.name();
        }

        BigDecimal leftPrice = left.lowestPrice() == null ? BigDecimal.ZERO : left.lowestPrice();
        BigDecimal rightPrice = right.lowestPrice() == null ? BigDecimal.ZERO : right.lowestPrice();
        if (leftPrice.compareTo(rightPrice) <= 0) {
            return left.name();
        }
        return right.name();
    }

    private int metric(CatalogComponentView view) {
        if (view.vramSizeGb() != null) {
            return view.vramSizeGb();
        }
        if (view.tdpWattage() != null) {
            return view.tdpWattage();
        }
        if (view.capacityGb() != null) {
            return view.capacityGb();
        }
        return 0;
    }

    private void addDiff(List<String> differences, String label, String left, String right) {
        if (left == null && right == null) {
            return;
        }
        if (left != null && left.equalsIgnoreCase(right)) {
            return;
        }
        differences.add(label + ": " + left + " vs " + right);
    }

    private boolean sameIgnoreCase(String left, String right) {
        return left == null ? right == null : left.equalsIgnoreCase(right);
    }
}