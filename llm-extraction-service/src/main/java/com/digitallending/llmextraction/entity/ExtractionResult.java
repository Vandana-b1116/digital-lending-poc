package com.digitallending.llmextraction.entity;

import com.digitallending.llmextraction.model.ExtractionReviewStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "extraction_results")
@Data
@NoArgsConstructor
public class ExtractionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "pay_period_start")
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end")
    private LocalDate payPeriodEnd;

    @Column(name = "gross_pay", precision = 15, scale = 2)
    private BigDecimal grossPay;

    @Column(name = "net_pay", precision = 15, scale = 2)
    private BigDecimal netPay;

    @Column(name = "ytd_gross", precision = 15, scale = 2)
    private BigDecimal ytdGross;

    @Column(name = "ytd_net", precision = 15, scale = 2)
    private BigDecimal ytdNet;

    @Column(name = "pay_frequency", length = 50)
    private String payFrequency;

    @Column(name = "llm_model_used", length = 100)
    private String llmModelUsed;

    @Column(name = "llm_prompt_version", length = 50)
    private String llmPromptVersion;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "raw_llm_response_json", columnDefinition = "TEXT")
    private String rawLlmResponseJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "extraction_review_status")
    private ExtractionReviewStatus status = ExtractionReviewStatus.PENDING_REVIEW;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "extractionResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscrepancyFlag> discrepancyFlags = new ArrayList<>();

    @OneToMany(mappedBy = "extractionResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FieldApproval> fieldApprovals = new ArrayList<>();
}
