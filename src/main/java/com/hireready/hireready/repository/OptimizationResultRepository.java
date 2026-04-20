package com.hireready.hireready.repository;

import com.hireready.hireready.entity.OptimizationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptimizationResultRepository extends JpaRepository<OptimizationResult, Long> {

    List<OptimizationResult> findAllByResumeId(Long resumeId);

    List<OptimizationResult> findAllByResumeUserId(Long userId);
}
