package com.backend.repository;

import com.backend.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    Optional<KnowledgeBase> findFirstByKeywordIgnoreCaseOrderByUpdatedAtDesc(String keyword);

    Optional<KnowledgeBase> findFirstByKeywordIgnoreCaseAndCategoryIgnoreCaseOrderBySearchCountDescUpdatedAtDesc(String keyword, String category);

    List<KnowledgeBase> findTop10ByKeywordContainingIgnoreCaseOrderBySearchCountDescUpdatedAtDesc(String keyword);

    List<KnowledgeBase> findTop10ByCategoryIgnoreCaseOrderBySearchCountDescUpdatedAtDesc(String category);
}