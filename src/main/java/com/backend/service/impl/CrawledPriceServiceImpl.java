package com.backend.service.impl;

import com.backend.entity.BaseComponent;
import com.backend.entity.CrawledPrice;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.BaseComponentRepository;
import com.backend.repository.CrawledPriceRepository;
import com.backend.service.CrawledPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CrawledPriceServiceImpl implements CrawledPriceService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final Pattern VND_PRICE_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,3}(?:[\\.,]\\d{3})+|\\d+)(?:\\s*₫)?");
    private static final Pattern JSON_PRICE_PATTERN = Pattern.compile("(?i)\\\"price\\\"\\s*[:=]\\s*\\\"?(\\d[\\d\\.]*)\\\"?");
    private static final Pattern META_PRICE_PATTERN = Pattern.compile("(?i)(?:price:amount|og:price:amount|product:price:amount)[^>]*content=\\\"?(\\d[\\d\\.]*)");

    private final CrawledPriceRepository repository;
    private final BaseComponentRepository baseComponentRepository;

    @Override
    public List<CrawledPrice> findAll() {
        return repository.findAll();
    }

    @Override
    public List<CrawledPrice> findByComponentId(Long componentId) {
        return repository.findAllByComponent_Id(componentId);
    }

    @Override
    public List<String> findDistinctSourceNames() {
        return repository.findDistinctSourceNames();
    }

    @Override
    public CrawledPrice findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay crawled price co id = " + id));
    }

    @Override
    public CrawledPrice create(CrawledPrice crawledPrice) {
        normalizeComponent(crawledPrice);
        crawledPrice.setId(null);
        if (crawledPrice.getCrawledAt() == null) {
            crawledPrice.setCrawledAt(LocalDateTime.now());
        }
        return repository.save(crawledPrice);
    }

    @Override
    public CrawledPrice update(Long id, CrawledPrice crawledPrice) {
        normalizeComponent(crawledPrice);
        crawledPrice.setId(id);
        if (crawledPrice.getCrawledAt() == null) {
            crawledPrice.setCrawledAt(LocalDateTime.now());
        }
        return repository.save(crawledPrice);
    }

    @Override
    public CrawledPrice crawlAndSave(Long componentId, String sourceUrl, String sourceName) {
        BaseComponent component = baseComponentRepository.findById(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay component co id = " + componentId));

        String html = fetchHtml(sourceUrl);
        BigDecimal priceValue = extractPrice(sourceUrl, html);

        CrawledPrice crawledPrice = new CrawledPrice();
        crawledPrice.setComponent(component);
        crawledPrice.setSourceName(normalizeSourceName(sourceName, sourceUrl));
        crawledPrice.setPriceValue(priceValue);
        crawledPrice.setCrawledAt(LocalDateTime.now());
        return repository.save(crawledPrice);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void normalizeComponent(CrawledPrice crawledPrice) {
        if (crawledPrice.getComponent() != null && crawledPrice.getComponent().getId() != null) {
            BaseComponent component = baseComponentRepository.findById(crawledPrice.getComponent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay component co id = " + crawledPrice.getComponent().getId()));
            crawledPrice.setComponent(component);
        }
    }

    private String fetchHtml(String sourceUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(sourceUrl))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new IllegalArgumentException("Khong the crawl gia tu URL: HTTP " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Khong the crawl gia tu URL: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Khong the crawl gia tu URL: " + ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("URL khong hop le hoac khong the crawl gia.", ex);
        }
    }

    private BigDecimal extractPrice(String sourceUrl, String html) {
        String normalizedHtml = html.replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<style.*?</style>", " ")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String candidate = firstMatch(META_PRICE_PATTERN, html);
        if (candidate == null) {
            candidate = firstMatch(JSON_PRICE_PATTERN, html);
        }

        if (candidate == null && isGearVn(sourceUrl)) {
            int marker = normalizedHtml.indexOf("Xem đánh giá");
            if (marker >= 0) {
                candidate = firstMatch(VND_PRICE_PATTERN, normalizedHtml.substring(marker));
            }
        }

        if (candidate == null) {
            candidate = firstMatch(VND_PRICE_PATTERN, normalizedHtml);
        }

        if (candidate == null) {
            throw new IllegalArgumentException("Khong tim thay gia tren trang crawl.");
        }

        return new BigDecimal(candidate.replaceAll("[^\\d]", ""));
    }

    private String firstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isGearVn(String sourceUrl) {
        try {
            String host = URI.create(sourceUrl).getHost();
            return host != null && host.toLowerCase().contains("gearvn.com");
        } catch (Exception ex) {
            return false;
        }
    }

    private String resolveSourceName(String sourceUrl) {
        try {
            String host = URI.create(sourceUrl).getHost();
            if (host == null) return sourceUrl;
            String normalized = host.toLowerCase();
            if (normalized.contains("gearvn")) return "GearVN";
            if (normalized.contains("phongvu")) return "Phong Vũ";
            if (normalized.contains("hacom")) return "HACOM";
            if (normalized.contains("anphatpc") || normalized.contains("anphat")) return "An Phát";
            if (normalized.contains("tinhocngoisao")) return "Tin Học Ngôi Sao";
            return host.replaceFirst("^www\\.", "");
        } catch (Exception ex) {
            return sourceUrl;
        }
    }

    private String normalizeSourceName(String sourceName, String sourceUrl) {
        if (sourceName != null && !sourceName.isBlank()) {
            return sourceName.trim();
        }
        return resolveSourceName(sourceUrl);
    }
}