package com.backend.repository;

import com.backend.entity.Gpu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpuRepository extends JpaRepository<Gpu, Long> {
}