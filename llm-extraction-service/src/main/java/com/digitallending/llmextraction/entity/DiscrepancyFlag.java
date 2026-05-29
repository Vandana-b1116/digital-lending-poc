package com.digitallending.llmextraction.entity;

import com.digitallending.llmextraction.model.DiscrepancyFlagType;
import com.digitallending.llmextraction.model.DiscrepancySeverity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "discrepancy_flags")
@Data
@NoArgsConstructor
public class DiscrepancyFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extraction_id", nullable = false)
    private ExtractionResult extractionResult;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "extracted_value", length = 500)
    private String extractedValue;

    @Column(name = "self_reported_value", length = 500)
    private String selfReportedValue;

    @Column(name = "variance_percentage", precision = 8, scale = 4)
    private BigDecimal variancePercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, columnDefinition = "discrepancy_severity")
    private DiscrepancySeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", nullable = false, columnDefinition = "discrepancy_flag_type")
    private DiscrepancyFlagType flagType;
}
