package com.digitallending.document.dto;

import com.digitallending.document.model.DocumentType;
import com.digitallending.document.model.ExtractionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentResponse {
    private Long id;
    private Long applicationId;
    private DocumentType documentType;
    private String originalFilename;
    private String mimeType;
    private Long fileSizeBytes;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private ExtractionStatus extractionStatus;
}
