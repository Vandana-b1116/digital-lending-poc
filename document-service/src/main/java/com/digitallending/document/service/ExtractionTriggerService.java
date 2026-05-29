package com.digitallending.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionTriggerService {

    private final RestClient.Builder restClientBuilder;

    @Value("${llm.service.url}")
    private String llmServiceUrl;

    public void triggerExtraction(Long documentId, Long applicationId) {
        try {
            restClientBuilder.baseUrl(llmServiceUrl).build()
                    .post()
                    .uri("/api/extract")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("documentId", documentId, "applicationId", applicationId))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Triggered LLM extraction for document={}, application={}", documentId, applicationId);
        } catch (Exception e) {
            log.warn("Failed to trigger LLM extraction (service may be down): {}", e.getMessage());
        }
    }
}
