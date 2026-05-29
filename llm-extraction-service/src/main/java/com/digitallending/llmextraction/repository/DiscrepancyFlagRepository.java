package com.digitallending.llmextraction.repository;

import com.digitallending.llmextraction.entity.DiscrepancyFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscrepancyFlagRepository extends JpaRepository<DiscrepancyFlag, Long> {

    List<DiscrepancyFlag> findByExtractionResultId(Long extractionId);
}
