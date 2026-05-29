package com.digitallending.llmextraction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FieldReviewRequest {

    @NotBlank
    private String fieldName;

    @NotNull
    private String action;

    private String finalValue;

    private String notes;
}
