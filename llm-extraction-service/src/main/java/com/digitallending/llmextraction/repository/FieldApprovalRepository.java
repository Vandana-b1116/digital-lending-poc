package com.digitallending.llmextraction.repository;

import com.digitallending.llmextraction.entity.FieldApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FieldApprovalRepository extends JpaRepository<FieldApproval, Long> {

    List<FieldApproval> findByExtractionResultId(Long extractionId);
}
