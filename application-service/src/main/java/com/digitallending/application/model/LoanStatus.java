package com.digitallending.application.model;

public enum LoanStatus {
    SUBMITTED,
    DOCUMENTS_UPLOADED,
    AI_EXTRACTION_COMPLETE,
    OFFICER_REVIEW_PENDING,
    OFFICER_APPROVED,
    SENT_TO_UNDERWRITING,
    DECISION_RECEIVED
}
