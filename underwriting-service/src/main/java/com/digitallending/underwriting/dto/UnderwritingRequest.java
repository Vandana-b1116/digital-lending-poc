package com.digitallending.underwriting.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnderwritingRequest {
    private Long applicationId;
    private String applicantName;
    private BigDecimal verifiedAnnualIncome;
    private String verifiedEmployer;
    private BigDecimal loanAmount;
}
