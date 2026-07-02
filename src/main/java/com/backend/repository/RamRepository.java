package com.backend.repository;

import com.backend.entity.Ram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RamRepository extends JpaRepository<Ram, Long> {
}