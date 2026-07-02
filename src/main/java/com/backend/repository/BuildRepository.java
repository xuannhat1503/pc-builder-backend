package com.backend.repository;

import com.backend.entity.Build;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildRepository extends JpaRepository<Build, Long> {

    List<Build> findAllByUser_Id(Long userId);
}