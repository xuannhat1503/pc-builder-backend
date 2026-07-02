package com.backend.repository;

import com.backend.entity.Mainboard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MainboardRepository extends JpaRepository<Mainboard, Long> {
}