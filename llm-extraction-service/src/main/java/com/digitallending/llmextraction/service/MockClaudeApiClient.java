package com.digitallending.llmextraction.service;

import com.digitallending.llmextraction.dto.ClaudeLlmResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
@Slf4j
@RequiredArgsConstructor
public class MockClaudeApiClient implements ClaudeApiClient {

    private static final String MOCK_JSON = """
            {
              "employer_name": "Acme Corporation",
              "pay_period_start": "2024-01-01",
              "pay_period_end": "2024-01-15",
              "gross_pay": 4583.33,
              "net_pay": 3250.00,
              "ytd_gross": 4583.33,
              "ytd_net": 3250.00,
              "pay_frequency": "SEMI_MONTHLY",
              "confidence_score": 0.92,
              "notes": "All fields extracted successfully from pay stub."
            }""";

    private final ObjectMapper objectMapper;

    @Override
    public ClaudeApiResult extract(String redactedText) {
        log.info("Using MOCK Claude API client — returning hardcoded response");
        try {
            ClaudeLlmResponse parsed = objectMapper.readValue(MOCK_JSON, ClaudeLlmResponse.class);
            return new ClaudeApiResult(parsed, MOCK_JSON, "mock-claude-sonnet");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse mock response", e);
        }
    }
}
