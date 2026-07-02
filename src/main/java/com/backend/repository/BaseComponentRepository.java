package com.backend.repository;

import com.backend.entity.BaseComponent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseComponentRepository extends JpaRepository<BaseComponent, Long> {
}