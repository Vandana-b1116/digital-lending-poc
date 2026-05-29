package com.digitallending.llmextraction.dto;

import com.digitallending.llmextraction.model.ExtractionReviewStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExtractionResponse {

    private Long id;
    private Long documentId;
    private Long applicationId;
    private String employerName;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private BigDecimal grossPay;
    private BigDecimal netPay;
    private BigDecimal ytdGross;
    private BigDecimal ytdNet;
    private String payFrequency;
    private String llmModelUsed;
    private String llmPromptVersion;
    private BigDecimal confidenceScore;
    private ExtractionReviewStatus status;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private List<DiscrepancyFlagDto> discrepancyFlags;
    private List<FieldApprovalDto> fieldApprovals;

    @Data
    public static class DiscrepancyFlagDto {
        private Long id;
        private String fieldName;
        private String extractedValue;
        private String selfReportedValue;
        private BigDecimal variancePercentage;
        private String severity;
        private String flagType;
    }

    @Data
    public static class FieldApprovalDto {
        private Long id;
        private String fieldName;
        private String finalValue;
        private String officerAction;
        private String officerNotes;
        private String approvedBy;
        private LocalDateTime approvedAt;
    }
}
