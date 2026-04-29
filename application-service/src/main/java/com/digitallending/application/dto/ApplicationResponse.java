package com.digitallending.application.dto;

import com.digitallending.application.model.LoanStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private String applicantName;
    private String applicantEmail;
    private BigDecimal selfReportedAnnualIncome;
    private String selfReportedEmployer;
    private BigDecimal loanAmountRequested;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
