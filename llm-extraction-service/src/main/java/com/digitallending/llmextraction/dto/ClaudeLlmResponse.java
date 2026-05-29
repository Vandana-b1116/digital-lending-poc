package com.digitallending.llmextraction.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeLlmResponse {

    @JsonProperty("employer_name")
    private String employerName;

    @JsonProperty("pay_period_start")
    private String payPeriodStart;

    @JsonProperty("pay_period_end")
    private String payPeriodEnd;

    @JsonProperty("gross_pay")
    private BigDecimal grossPay;

    @JsonProperty("net_pay")
    private BigDecimal netPay;

    @JsonProperty("ytd_gross")
    private BigDecimal ytdGross;

    @JsonProperty("ytd_net")
    private BigDecimal ytdNet;

    @JsonProperty("pay_frequency")
    private String payFrequency;

    @JsonProperty("confidence_score")
    private BigDecimal confidenceScore;

    private String notes;
}
