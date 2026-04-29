package com.digitallending.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UnderwritingRequestDto {
    private Long applicationId;
    private String applicantName;
    private BigDecimal verifiedAnnualIncome;
    private String verifiedEmployer;
    private BigDecimal loanAmount;
}
