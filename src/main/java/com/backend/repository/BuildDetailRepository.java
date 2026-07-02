package com.backend.repository;

import com.backend.entity.BuildDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildDetailRepository extends JpaRepository<BuildDetail, Long> {

    List<BuildDetail> findAllByBuild_Id(Long buildId);
}