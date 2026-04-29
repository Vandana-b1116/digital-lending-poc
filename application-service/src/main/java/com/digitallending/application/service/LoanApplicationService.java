package com.digitallending.application.service;

import com.digitallending.application.dto.*;
import com.digitallending.application.entity.LoanApplication;
import com.digitallending.application.model.LoanStatus;
import com.digitallending.application.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationService {

    private final LoanApplicationRepository repository;
    private final RestClient.Builder restClientBuilder;

    @Value("${underwriting.service.url}")
    private String underwritingServiceUrl;

    public List<ApplicationResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ApplicationResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ApplicationResponse create(CreateApplicationRequest request) {
        LoanApplication app = new LoanApplication();
        app.setApplicantName(request.getApplicantName());
        app.setApplicantEmail(request.getApplicantEmail());
        app.setSelfReportedAnnualIncome(request.getSelfReportedAnnualIncome());
        app.setSelfReportedEmployer(request.getSelfReportedEmployer());
        app.setLoanAmountRequested(request.getLoanAmountRequested());
        app.setStatus(LoanStatus.SUBMITTED);
        return toResponse(repository.save(app));
    }

    @Transactional
    public ApplicationResponse updateStatus(Long id, StatusUpdateRequest request) {
        LoanApplication app = getOrThrow(id);
        app.setStatus(request.getStatus());
        return toResponse(repository.save(app));
    }

    @Transactional
    public ApplicationResponse submitToUnderwriting(Long id) {
        LoanApplication app = getOrThrow(id);
        app.setStatus(LoanStatus.SENT_TO_UNDERWRITING);
        repository.save(app);

        UnderwritingRequestDto uwRequest = new UnderwritingRequestDto(
                app.getId(),
                app.getApplicantName(),
                app.getSelfReportedAnnualIncome(),
                app.getSelfReportedEmployer(),
                app.getLoanAmountRequested()
        );

        try {
            RestClient client = restClientBuilder.baseUrl(underwritingServiceUrl).build();
            client.post()
                    .uri("/api/underwriting/decisions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(uwRequest)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Submitted application {} to underwriting", id);
        } catch (Exception e) {
            log.warn("Underwriting call failed (stub may be down): {}", e.getMessage());
        }

        app.setStatus(LoanStatus.DECISION_RECEIVED);
        return toResponse(repository.save(app));
    }

    private LoanApplication getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Application not found: " + id));
    }

    private ApplicationResponse toResponse(LoanApplication app) {
        ApplicationResponse r = new ApplicationResponse();
        r.setId(app.getId());
        r.setApplicantName(app.getApplicantName());
        r.setApplicantEmail(app.getApplicantEmail());
        r.setSelfReportedAnnualIncome(app.getSelfReportedAnnualIncome());
        r.setSelfReportedEmployer(app.getSelfReportedEmployer());
        r.setLoanAmountRequested(app.getLoanAmountRequested());
        r.setStatus(app.getStatus());
        r.setCreatedAt(app.getCreatedAt());
        r.setUpdatedAt(app.getUpdatedAt());
        return r;
    }
}
