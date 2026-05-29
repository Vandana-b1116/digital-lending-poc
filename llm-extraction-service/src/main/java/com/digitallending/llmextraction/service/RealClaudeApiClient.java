package com.digitallending.llmextraction.service;

import com.digitallending.llmextraction.dto.ClaudeLlmResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Profile("!mock")
@Slf4j
public class RealClaudeApiClient implements ClaudeApiClient {

    private static final String PROMPT_TEMPLATE = """
            You are a financial document parser. Extract the following fields from this pay stub \
            and return ONLY valid JSON with no additional text:
            {
              "employer_name": "string",
              "pay_period_start": "YYYY-MM-DD",
              "pay_period_end": "YYYY-MM-DD",
              "gross_pay": number,
              "net_pay": number,
              "ytd_gross": number,
              "ytd_net": number,
              "pay_frequency": "WEEKLY|BI_WEEKLY|SEMI_MONTHLY|MONTHLY",
              "confidence_score": 0.0-1.0,
              "notes": "string (explain any null fields)"
            }

            Pay stub text:
            %s""";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxTokens;
    private final int timeoutSeconds;

    public RealClaudeApiClient(
            @Value("${claude.api.url}") String apiUrl,
            @Value("${claude.api.key}") String apiKey,
            @Value("${claude.model}") String model,
            @Value("${claude.max.tokens}") int maxTokens,
            @Value("${claude.timeout.seconds}") int timeoutSeconds,
            ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
        this.maxTokens = maxTokens;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public ClaudeApiResult extract(String redactedText) {
        String prompt = String.format(PROMPT_TEMPLATE, redactedText);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        log.info("Calling Claude API with model={}", model);

        String rawResponse = callWithRetry(requestBody);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String contentText = root.path("content").get(0).path("text").asText();
            ClaudeLlmResponse parsed = objectMapper.readValue(contentText, ClaudeLlmResponse.class);
            log.info("Claude API response parsed successfully, confidence={}", parsed.getConfidenceScore());
            return new ClaudeApiResult(parsed, rawResponse, model);
        } catch (Exception e) {
            log.error("Failed to parse Claude API response", e);
            throw new RuntimeException("Failed to parse Claude API response: " + e.getMessage(), e);
        }
    }

    private String callWithRetry(Map<String, Object> requestBody) {
        try {
            return doCall(requestBody);
        } catch (Exception e) {
            log.warn("Claude API call failed, retrying once: {}", e.getMessage());
            return doCall(requestBody);
        }
    }

    private String doCall(Map<String, Object> requestBody) {
        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();
    }
}
