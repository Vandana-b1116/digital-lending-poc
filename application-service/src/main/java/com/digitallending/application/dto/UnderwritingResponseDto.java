package com.digitallending.application.dto;

import lombok.Data;

@Data
public class UnderwritingResponseDto {
    private Long applicationId;
    private String decision;
    private String reason;
}
