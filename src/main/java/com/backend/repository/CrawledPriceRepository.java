package com.backend.repository;

import com.backend.entity.CrawledPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CrawledPriceRepository extends JpaRepository<CrawledPrice, Long> {

    List<CrawledPrice> findAllByComponent_Id(Long componentId);

    @Query("select distinct cp.sourceName from CrawledPrice cp order by cp.sourceName")
    List<String> findDistinctSourceNames();
}