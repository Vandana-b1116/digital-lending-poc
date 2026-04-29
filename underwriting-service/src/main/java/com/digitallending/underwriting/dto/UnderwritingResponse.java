package com.digitallending.underwriting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnderwritingResponse {
    private Long applicationId;
    private String decision;
    private String reason;
}
