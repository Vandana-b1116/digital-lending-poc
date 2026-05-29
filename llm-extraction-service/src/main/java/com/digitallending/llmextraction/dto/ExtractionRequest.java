package com.digitallending.llmextraction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExtractionRequest {

    @NotNull
    private Long documentId;

    @NotNull
    private Long applicationId;
}
