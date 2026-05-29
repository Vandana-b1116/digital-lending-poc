package com.digitallending.llmextraction.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplicationDataDto {

    private Long id;
    private String applicantName;
    private String applicantEmail;
    private BigDecimal selfReportedAnnualIncome;
    private String selfReportedEmployer;
    private BigDecimal loanAmountRequested;
    private String status;
}
