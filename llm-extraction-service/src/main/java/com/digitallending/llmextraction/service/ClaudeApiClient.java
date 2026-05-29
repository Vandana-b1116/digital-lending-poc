package com.digitallending.llmextraction.service;

import com.digitallending.llmextraction.dto.ClaudeLlmResponse;

public interface ClaudeApiClient {

    ClaudeApiResult extract(String redactedText);

    record ClaudeApiResult(
            ClaudeLlmResponse parsed,
            String rawJson,
            String modelUsed
    ) {}
}
