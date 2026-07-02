package com.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnTransformer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "crawled_prices")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CrawledPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private BaseComponent component;

    @Column(name = "source_name", nullable = false, length = 255)
    private String sourceName;

    @Column(name = "price_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceValue;

    @ColumnTransformer(read = "nullif(crawled_at, '0000-00-00 00:00:00')")
    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;
}