package com.backend.repository;

import com.backend.entity.CrawledPrice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CrawledPriceRepository extends JpaRepository<CrawledPrice, Long> {

    List<CrawledPrice> findAllByComponent_Id(Long componentId);

    @Query("select distinct cp.sourceName from CrawledPrice cp order by cp.sourceName")
    List<String> findDistinctSourceNames();

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE crawled_prices
            SET crawled_at = CURRENT_TIMESTAMP
            WHERE crawled_at IS NULL
               OR crawled_at = '0000-00-00 00:00:00'
               OR crawled_at = '0000-00-00 00:00:00.000000'
            """, nativeQuery = true)
    int normalizeInvalidCrawledAtValues();
}