-- ============================================================
-- Digital Lending POC — PostgreSQL Schema
-- ============================================================

-- ── Application Service Tables ──────────────────────────────

CREATE TYPE loan_status AS ENUM (
    'SUBMITTED',
    'DOCUMENTS_UPLOADED',
    'AI_EXTRACTION_COMPLETE',
    'OFFICER_REVIEW_PENDING',
    'OFFICER_APPROVED',
    'SENT_TO_UNDERWRITING',
    'DECISION_RECEIVED'
);

CREATE TABLE loan_applications (
    id                        BIGSERIAL PRIMARY KEY,
    applicant_name            VARCHAR(255)   NOT NULL,
    applicant_email           VARCHAR(255)   NOT NULL,
    self_reported_annual_income NUMERIC(15,2) NOT NULL,
    self_reported_employer    VARCHAR(255)   NOT NULL,
    loan_amount_requested     NUMERIC(15,2)  NOT NULL,
    status                    loan_status    NOT NULL DEFAULT 'SUBMITTED',
    created_at                TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── Document Service Tables ──────────────────────────────────

CREATE TYPE document_type AS ENUM ('PAY_STUB');

CREATE TYPE extraction_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETE', 'FAILED');

CREATE TABLE documents (
    id                  BIGSERIAL PRIMARY KEY,
    application_id      BIGINT          NOT NULL REFERENCES loan_applications(id),
    document_type       document_type   NOT NULL DEFAULT 'PAY_STUB',
    original_filename   VARCHAR(500)    NOT NULL,
    storage_path        VARCHAR(1000)   NOT NULL,
    mime_type           VARCHAR(100),
    file_size_bytes     BIGINT,
    uploaded_by         VARCHAR(255),
    uploaded_at         TIMESTAMP       NOT NULL DEFAULT NOW(),
    extraction_status   extraction_status NOT NULL DEFAULT 'PENDING'
);

-- ── LLM Extraction Service Tables ───────────────────────────

CREATE TYPE extraction_review_status AS ENUM (
    'PENDING_REVIEW',
    'APPROVED',
    'REJECTED',
    'PARTIALLY_APPROVED'
);

CREATE TYPE discrepancy_severity AS ENUM ('LOW', 'MEDIUM', 'HIGH');

CREATE TYPE discrepancy_flag_type AS ENUM (
    'INCOME_MISMATCH',
    'EMPLOYER_MISMATCH',
    'MISSING_FIELD'
);

CREATE TYPE officer_action AS ENUM ('ACCEPTED', 'EDITED', 'REJECTED');

CREATE TABLE extraction_results (
    id                      BIGSERIAL PRIMARY KEY,
    document_id             BIGINT          NOT NULL REFERENCES documents(id),
    application_id          BIGINT          NOT NULL REFERENCES loan_applications(id),
    employer_name           VARCHAR(255),
    pay_period_start        DATE,
    pay_period_end          DATE,
    gross_pay               NUMERIC(15,2),
    net_pay                 NUMERIC(15,2),
    ytd_gross               NUMERIC(15,2),
    ytd_net                 NUMERIC(15,2),
    pay_frequency           VARCHAR(50),
    llm_model_used          VARCHAR(100),
    llm_prompt_version      VARCHAR(50),
    confidence_score        NUMERIC(5,4),
    raw_llm_response_json   TEXT,
    status                  extraction_review_status NOT NULL DEFAULT 'PENDING_REVIEW',
    reviewed_by             VARCHAR(255),
    reviewed_at             TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE discrepancy_flags (
    id                    BIGSERIAL PRIMARY KEY,
    extraction_id         BIGINT              NOT NULL REFERENCES extraction_results(id),
    field_name            VARCHAR(100)        NOT NULL,
    extracted_value       VARCHAR(500),
    self_reported_value   VARCHAR(500),
    variance_percentage   NUMERIC(8,4),
    severity              discrepancy_severity NOT NULL,
    flag_type             discrepancy_flag_type NOT NULL
);

CREATE TABLE field_approvals (
    id              BIGSERIAL PRIMARY KEY,
    extraction_id   BIGINT          NOT NULL REFERENCES extraction_results(id),
    field_name      VARCHAR(100)    NOT NULL,
    final_value     VARCHAR(500),
    officer_action  officer_action  NOT NULL,
    officer_notes   TEXT,
    approved_by     VARCHAR(255),
    approved_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (extraction_id, field_name)
);

-- ── Underwriting Service Tables ──────────────────────────────

CREATE TYPE underwriting_decision AS ENUM ('APPROVED', 'DENIED', 'MANUAL_REVIEW');

CREATE TABLE underwriting_decisions (
    id                      BIGSERIAL PRIMARY KEY,
    application_id          BIGINT               NOT NULL REFERENCES loan_applications(id),
    applicant_name          VARCHAR(255),
    verified_annual_income  NUMERIC(15,2),
    verified_employer       VARCHAR(255),
    loan_amount             NUMERIC(15,2),
    credit_score            INTEGER,
    decision                underwriting_decision NOT NULL,
    reason                  TEXT,
    decided_at              TIMESTAMP            NOT NULL DEFAULT NOW()
);

-- ── Indexes ──────────────────────────────────────────────────

CREATE INDEX idx_documents_application_id ON documents(application_id);
CREATE INDEX idx_extraction_results_document_id ON extraction_results(document_id);
CREATE INDEX idx_extraction_results_application_id ON extraction_results(application_id);
CREATE INDEX idx_discrepancy_flags_extraction_id ON discrepancy_flags(extraction_id);
CREATE INDEX idx_field_approvals_extraction_id ON field_approvals(extraction_id);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);

-- ── Seed Data (test loan officer user reference only) ────────
-- Actual user auth is handled by Spring Security in-memory config.
-- No users table needed for POC.
