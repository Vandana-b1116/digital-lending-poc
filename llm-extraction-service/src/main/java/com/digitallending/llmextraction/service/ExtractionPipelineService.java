package com.digitallending.llmextraction.service;

import com.digitallending.llmextraction.dto.ApplicationDataDto;
import com.digitallending.llmextraction.dto.ClaudeLlmResponse;
import com.digitallending.llmextraction.entity.DiscrepancyFlag;
import com.digitallending.llmextraction.entity.ExtractionResult;
import com.digitallending.llmextraction.model.ExtractionReviewStatus;
import com.digitallending.llmextraction.repository.ExtractionResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionPipelineService {

    private static final String PROMPT_VERSION = "v1.0";

    private final ExtractionResultRepository extractionResultRepository;
    private final TextExtractionService textExtractionService;
    private final PiiRedactionService piiRedactionService;
    private final ClaudeApiClient claudeApiClient;
    private final DiscrepancyDetectionService discrepancyDetectionService;
    private final RestClient.Builder restClientBuilder;

    @Value("${document.service.url}")
    private String documentServiceUrl;

    @Value("${application.service.url}")
    private String applicationServiceUrl;

    @Async("extractionExecutor")
    public void runPipeline(Long documentId, Long applicationId) {
        log.info("Starting extraction pipeline for document={}, application={}", documentId, applicationId);

        try {
            // Step 1 — Update document status to IN_PROGRESS
            updateDocumentExtractionStatus(documentId, "IN_PROGRESS");

            // Step 2 — Fetch document content and extract text
            String mimeType = getDocumentMimeType(documentId);
            InputStream fileStream = fetchDocumentContent(documentId);
            String rawText = textExtractionService.extractText(fileStream, mimeType);
            log.info("Step 2 complete — extracted {} chars from document {}", rawText.length(), documentId);

            // Step 3 — Redact PII
            ApplicationDataDto appData = fetchApplicationData(applicationId);
            PiiRedactionService.RedactionResult redactionResult =
                    piiRedactionService.redact(rawText, appData.getApplicantName());
            log.info("Step 3 complete — {} total PII redactions", redactionResult.totalRedactions());

            // Step 4 — Call Claude API
            ClaudeApiClient.ClaudeApiResult llmResult = claudeApiClient.extract(redactionResult.redactedText());
            ClaudeLlmResponse llmData = llmResult.parsed();
            log.info("Step 4 complete — LLM extraction done, confidence={}", llmData.getConfidenceScore());

            // Step 5 — Build ExtractionResult and detect discrepancies
            ExtractionResult extraction = buildExtractionResult(documentId, applicationId, llmData, llmResult);
            List<DiscrepancyFlag> flags = discrepancyDetectionService.detect(extraction, llmData, appData);
            extraction.getDiscrepancyFlags().addAll(flags);
            log.info("Step 5 complete — {} discrepancy flags", flags.size());

            // Step 6 — Persist and notify
            extractionResultRepository.save(extraction);
            updateDocumentExtractionStatus(documentId, "COMPLETE");
            updateApplicationStatus(applicationId, "AI_EXTRACTION_COMPLETE");
            log.info("Pipeline complete for document={}, extraction={}", documentId, extraction.getId());

        } catch (Exception e) {
            log.error("Extraction pipeline failed for document={}", documentId, e);
            try {
                updateDocumentExtractionStatus(documentId, "FAILED");
            } catch (Exception callbackError) {
                log.error("Failed to update document status to FAILED", callbackError);
            }
        }
    }

    private ExtractionResult buildExtractionResult(Long documentId, Long applicationId,
                                                    ClaudeLlmResponse llmData,
                                                    ClaudeApiClient.ClaudeApiResult llmResult) {
        ExtractionResult result = new ExtractionResult();
        result.setDocumentId(documentId);
        result.setApplicationId(applicationId);
        result.setEmployerName(llmData.getEmployerName());
        result.setGrossPay(llmData.getGrossPay());
        result.setNetPay(llmData.getNetPay());
        result.setYtdGross(llmData.getYtdGross());
        result.setYtdNet(llmData.getYtdNet());
        result.setPayFrequency(llmData.getPayFrequency());
        result.setConfidenceScore(llmData.getConfidenceScore());
        result.setRawLlmResponseJson(llmResult.rawJson());
        result.setLlmModelUsed(llmResult.modelUsed());
        result.setLlmPromptVersion(PROMPT_VERSION);
        result.setStatus(ExtractionReviewStatus.PENDING_REVIEW);

        if (llmData.getPayPeriodStart() != null) {
            result.setPayPeriodStart(LocalDate.parse(llmData.getPayPeriodStart()));
        }
        if (llmData.getPayPeriodEnd() != null) {
            result.setPayPeriodEnd(LocalDate.parse(llmData.getPayPeriodEnd()));
        }

        return result;
    }

    private InputStream fetchDocumentContent(Long documentId) {
        try {
            Resource resource = restClientBuilder.baseUrl(documentServiceUrl).build()
                    .get()
                    .uri("/api/documents/{id}/content", documentId)
                    .retrieve()
                    .body(Resource.class);
            return resource.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch document content for id=" + documentId, e);
        }
    }

    private String getDocumentMimeType(Long documentId) {
        record DocMeta(String mimeType) {}
        DocMeta meta = restClientBuilder.baseUrl(documentServiceUrl).build()
                .get()
                .uri("/api/documents/{id}", documentId)
                .retrieve()
                .body(DocMeta.class);
        return meta != null ? meta.mimeType() : "application/pdf";
    }

    private ApplicationDataDto fetchApplicationData(Long applicationId) {
        return restClientBuilder.baseUrl(applicationServiceUrl).build()
                .get()
                .uri("/api/applications/{id}", applicationId)
                .retrieve()
                .body(ApplicationDataDto.class);
    }

    private void updateDocumentExtractionStatus(Long documentId, String status) {
        restClientBuilder.baseUrl(documentServiceUrl).build()
                .patch()
                .uri("/api/documents/{id}/extraction-status?status={status}", documentId, status)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
    }

    private void updateApplicationStatus(Long applicationId, String status) {
        try {
            restClientBuilder.baseUrl(applicationServiceUrl).build()
                    .patch()
                    .uri("/api/applications/{id}/status", applicationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of("status", status))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to update application {} status to {}: {}", applicationId, status, e.getMessage());
        }
    }
}
