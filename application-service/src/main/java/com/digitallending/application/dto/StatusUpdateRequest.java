package com.digitallending.application.dto;

import com.digitallending.application.model.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull
    private LoanStatus status;
}
