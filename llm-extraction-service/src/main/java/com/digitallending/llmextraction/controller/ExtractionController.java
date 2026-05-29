package com.digitallending.llmextraction.controller;

import com.digitallending.llmextraction.dto.*;
import com.digitallending.llmextraction.entity.DiscrepancyFlag;
import com.digitallending.llmextraction.entity.ExtractionResult;
import com.digitallending.llmextraction.entity.FieldApproval;
import com.digitallending.llmextraction.model.ExtractionReviewStatus;
import com.digitallending.llmextraction.model.OfficerAction;
import com.digitallending.llmextraction.repository.ExtractionResultRepository;
import com.digitallending.llmextraction.repository.FieldApprovalRepository;
import com.digitallending.llmextraction.service.ExtractionPipelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExtractionController {

    private final ExtractionPipelineService pipelineService;
    private final ExtractionResultRepository extractionResultRepository;
    private final FieldApprovalRepository fieldApprovalRepository;

    @PostMapping("/extract")
    public ResponseEntity<Void> triggerExtraction(@Valid @RequestBody ExtractionRequest request) {
        log.info("Extraction requested for document={}, application={}",
                request.getDocumentId(), request.getApplicationId());
        pipelineService.runPipeline(request.getDocumentId(), request.getApplicationId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/extractions/document/{documentId}")
    public ResponseEntity<ExtractionResponse> getByDocument(@PathVariable Long documentId) {
        return extractionResultRepository.findByDocumentId(documentId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/extractions/application/{applicationId}")
    public List<ExtractionResponse> getByApplication(@PathVariable Long applicationId) {
        return extractionResultRepository.findByApplicationId(applicationId)
                .stream().map(this::toResponse).toList();
    }

    @PostMapping("/extractions/{extractionId}/review")
    public ResponseEntity<ExtractionResponse> submitReview(
            @PathVariable Long extractionId,
            @Valid @RequestBody ReviewSubmission submission) {
        try {
            ExtractionResult extraction = extractionResultRepository.findById(extractionId)
                    .orElseThrow(() -> new NoSuchElementException("Extraction not found: " + extractionId));

            for (FieldReviewRequest fieldReview : submission.getFieldReviews()) {
                FieldApproval approval = new FieldApproval();
                approval.setExtractionResult(extraction);
                approval.setFieldName(fieldReview.getFieldName());
                approval.setOfficerAction(OfficerAction.valueOf(fieldReview.getAction()));
                approval.setFinalValue(fieldReview.getFinalValue());
                approval.setOfficerNotes(fieldReview.getNotes());
                approval.setApprovedBy(submission.getReviewedBy());
                extraction.getFieldApprovals().add(approval);
            }

            extraction.setReviewedBy(submission.getReviewedBy());
            extraction.setReviewedAt(LocalDateTime.now());
            extractionResultRepository.save(extraction);

            log.info("Review submitted for extraction={} by {}", extractionId, submission.getReviewedBy());
            return ResponseEntity.ok(toResponse(extraction));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/extractions/{extractionId}/approve")
    public ResponseEntity<?> approveExtraction(
            @PathVariable Long extractionId,
            @RequestParam String approvedBy) {
        try {
            ExtractionResult extraction = extractionResultRepository.findById(extractionId)
                    .orElseThrow(() -> new NoSuchElementException("Extraction not found: " + extractionId));

            List<DiscrepancyFlag> flags = extraction.getDiscrepancyFlags();
            List<FieldApproval> approvals = extraction.getFieldApprovals();

            Set<String> flaggedFields = flags.stream()
                    .map(DiscrepancyFlag::getFieldName)
                    .collect(Collectors.toSet());
            Set<String> reviewedFields = approvals.stream()
                    .map(FieldApproval::getFieldName)
                    .collect(Collectors.toSet());

            if (!reviewedFields.containsAll(flaggedFields)) {
                Set<String> unreviewed = flaggedFields.stream()
                        .filter(f -> !reviewedFields.contains(f))
                        .collect(Collectors.toSet());
                return ResponseEntity.badRequest().body(
                        java.util.Map.of("error", "All flagged fields must be reviewed before approval",
                                "unreviewedFields", unreviewed));
            }

            extraction.setStatus(ExtractionReviewStatus.APPROVED);
            extraction.setReviewedBy(approvedBy);
            extraction.setReviewedAt(LocalDateTime.now());
            extractionResultRepository.save(extraction);

            log.info("Extraction {} approved by {}", extractionId, approvedBy);
            return ResponseEntity.ok(toResponse(extraction));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ExtractionResponse toResponse(ExtractionResult entity) {
        ExtractionResponse r = new ExtractionResponse();
        r.setId(entity.getId());
        r.setDocumentId(entity.getDocumentId());
        r.setApplicationId(entity.getApplicationId());
        r.setEmployerName(entity.getEmployerName());
        r.setPayPeriodStart(entity.getPayPeriodStart());
        r.setPayPeriodEnd(entity.getPayPeriodEnd());
        r.setGrossPay(entity.getGrossPay());
        r.setNetPay(entity.getNetPay());
        r.setYtdGross(entity.getYtdGross());
        r.setYtdNet(entity.getYtdNet());
        r.setPayFrequency(entity.getPayFrequency());
        r.setLlmModelUsed(entity.getLlmModelUsed());
        r.setLlmPromptVersion(entity.getLlmPromptVersion());
        r.setConfidenceScore(entity.getConfidenceScore());
        r.setStatus(entity.getStatus());
        r.setReviewedBy(entity.getReviewedBy());
        r.setReviewedAt(entity.getReviewedAt());
        r.setCreatedAt(entity.getCreatedAt());

        r.setDiscrepancyFlags(entity.getDiscrepancyFlags().stream().map(flag -> {
            ExtractionResponse.DiscrepancyFlagDto dto = new ExtractionResponse.DiscrepancyFlagDto();
            dto.setId(flag.getId());
            dto.setFieldName(flag.getFieldName());
            dto.setExtractedValue(flag.getExtractedValue());
            dto.setSelfReportedValue(flag.getSelfReportedValue());
            dto.setVariancePercentage(flag.getVariancePercentage());
            dto.setSeverity(flag.getSeverity().name());
            dto.setFlagType(flag.getFlagType().name());
            return dto;
        }).toList());

        r.setFieldApprovals(entity.getFieldApprovals().stream().map(fa -> {
            ExtractionResponse.FieldApprovalDto dto = new ExtractionResponse.FieldApprovalDto();
            dto.setId(fa.getId());
            dto.setFieldName(fa.getFieldName());
            dto.setFinalValue(fa.getFinalValue());
            dto.setOfficerAction(fa.getOfficerAction().name());
            dto.setOfficerNotes(fa.getOfficerNotes());
            dto.setApprovedBy(fa.getApprovedBy());
            dto.setApprovedAt(fa.getApprovedAt());
            return dto;
        }).toList());

        return r;
    }
}
