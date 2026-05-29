package com.digitallending.llmextraction.repository;

import com.digitallending.llmextraction.entity.ExtractionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExtractionResultRepository extends JpaRepository<ExtractionResult, Long> {

    Optional<ExtractionResult> findByDocumentId(Long documentId);

    List<ExtractionResult> findByApplicationId(Long applicationId);
}
