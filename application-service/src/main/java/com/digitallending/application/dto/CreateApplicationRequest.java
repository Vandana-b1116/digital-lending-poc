package com.digitallending.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateApplicationRequest {

    @NotBlank
    private String applicantName;

    @NotBlank
    @Email
    private String applicantEmail;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal selfReportedAnnualIncome;

    @NotBlank
    private String selfReportedEmployer;

    @NotNull
    @DecimalMin(value = "1000.00")
    private BigDecimal loanAmountRequested;
}
