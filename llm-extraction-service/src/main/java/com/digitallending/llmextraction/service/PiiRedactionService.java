package com.digitallending.llmextraction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PiiRedactionService {

    private static final Pattern SSN_PATTERN = Pattern.compile("\\d{3}-\\d{2}-\\d{4}");
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("\\b\\d{8,17}\\b");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "\\d+\\s+[A-Za-z]+\\s+(St|Street|Ave|Avenue|Blvd|Boulevard|Dr|Drive|Ln|Lane|Rd|Road|Ct|Court|Way|Pl|Place)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public RedactionResult redact(String rawText, String applicantName) {
        int ssnCount = 0;
        int accountCount = 0;
        int nameCount = 0;
        int addressCount = 0;

        String text = rawText;

        Matcher ssnMatcher = SSN_PATTERN.matcher(text);
        ssnCount = countMatches(ssnMatcher);
        text = SSN_PATTERN.matcher(text).replaceAll("[REDACTED-SSN]");

        Matcher addressMatcher = ADDRESS_PATTERN.matcher(text);
        addressCount = countMatches(addressMatcher);
        text = ADDRESS_PATTERN.matcher(text).replaceAll("[REDACTED-ADDR]");

        // Redact account numbers after SSN to avoid double-matching
        Matcher accountMatcher = ACCOUNT_PATTERN.matcher(text);
        accountCount = countMatches(accountMatcher);
        text = ACCOUNT_PATTERN.matcher(text).replaceAll("[REDACTED-ACCT]");

        if (applicantName != null && !applicantName.isBlank()) {
            String[] nameParts = applicantName.trim().split("\\s+");
            for (String part : nameParts) {
                if (part.length() >= 2) {
                    Pattern namePattern = Pattern.compile(Pattern.quote(part), Pattern.CASE_INSENSITIVE);
                    nameCount += countMatches(namePattern.matcher(text));
                    text = namePattern.matcher(text).replaceAll("[APPLICANT]");
                }
            }
        }

        log.info("PII redaction complete — SSN: {}, accounts: {}, names: {}, addresses: {}",
                ssnCount, accountCount, nameCount, addressCount);

        return new RedactionResult(text, ssnCount, accountCount, nameCount, addressCount);
    }

    private int countMatches(Matcher matcher) {
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public record RedactionResult(
            String redactedText,
            int ssnRedactions,
            int accountRedactions,
            int nameRedactions,
            int addressRedactions
    ) {
        public int totalRedactions() {
            return ssnRedactions + accountRedactions + nameRedactions + addressRedactions;
        }
    }
}
