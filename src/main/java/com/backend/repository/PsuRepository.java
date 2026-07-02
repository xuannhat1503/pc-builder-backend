package com.backend.repository;

import com.backend.entity.Psu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PsuRepository extends JpaRepository<Psu, Long> {
}