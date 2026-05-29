package com.digitallending.llmextraction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReviewSubmission {

    @NotEmpty
    @Valid
    private List<FieldReviewRequest> fieldReviews;

    @NotBlank
    private String reviewedBy;
}
