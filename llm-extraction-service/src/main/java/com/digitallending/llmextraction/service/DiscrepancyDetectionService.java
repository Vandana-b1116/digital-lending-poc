package com.digitallending.llmextraction.service;

import com.digitallending.llmextraction.dto.ApplicationDataDto;
import com.digitallending.llmextraction.dto.ClaudeLlmResponse;
import com.digitallending.llmextraction.entity.DiscrepancyFlag;
import com.digitallending.llmextraction.entity.ExtractionResult;
import com.digitallending.llmextraction.model.DiscrepancyFlagType;
import com.digitallending.llmextraction.model.DiscrepancySeverity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DiscrepancyDetectionService {

    private static final Map<String, Integer> PAY_FREQUENCY_MULTIPLIERS = Map.of(
            "WEEKLY", 52,
            "BI_WEEKLY", 26,
            "SEMI_MONTHLY", 24,
            "MONTHLY", 12
    );

    private final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

    @Value("${discrepancy.income.high.threshold}")
    private double highThreshold;

    @Value("${discrepancy.income.medium.threshold}")
    private double mediumThreshold;

    @Value("${discrepancy.employer.similarity.threshold}")
    private double employerThreshold;

    public List<DiscrepancyFlag> detect(ExtractionResult extraction, ClaudeLlmResponse llmData, ApplicationDataDto appData) {
        List<DiscrepancyFlag> flags = new ArrayList<>();

        checkIncomeMismatch(extraction, llmData, appData, flags);
        checkEmployerMismatch(extraction, llmData, appData, flags);
        checkMissingFields(extraction, llmData, flags);

        log.info("Discrepancy detection complete — {} flags generated", flags.size());
        return flags;
    }

    private void checkIncomeMismatch(ExtractionResult extraction, ClaudeLlmResponse llmData,
                                     ApplicationDataDto appData, List<DiscrepancyFlag> flags) {
        if (llmData.getGrossPay() == null || llmData.getPayFrequency() == null) {
            return;
        }

        Integer multiplier = PAY_FREQUENCY_MULTIPLIERS.get(llmData.getPayFrequency().toUpperCase());
        if (multiplier == null) {
            log.warn("Unknown pay frequency: {}", llmData.getPayFrequency());
            return;
        }

        BigDecimal annualizedIncome = llmData.getGrossPay().multiply(BigDecimal.valueOf(multiplier));
        BigDecimal selfReported = appData.getSelfReportedAnnualIncome();

        if (selfReported == null || selfReported.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal variance = annualizedIncome.subtract(selfReported).abs()
                .divide(selfReported, 4, RoundingMode.HALF_UP);

        DiscrepancySeverity severity;
        if (variance.doubleValue() > highThreshold) {
            severity = DiscrepancySeverity.HIGH;
        } else if (variance.doubleValue() > mediumThreshold) {
            severity = DiscrepancySeverity.MEDIUM;
        } else {
            severity = DiscrepancySeverity.LOW;
        }

        DiscrepancyFlag flag = new DiscrepancyFlag();
        flag.setExtractionResult(extraction);
        flag.setFieldName("annual_income");
        flag.setExtractedValue(annualizedIncome.toPlainString());
        flag.setSelfReportedValue(selfReported.toPlainString());
        flag.setVariancePercentage(variance.multiply(BigDecimal.valueOf(100)));
        flag.setSeverity(severity);
        flag.setFlagType(DiscrepancyFlagType.INCOME_MISMATCH);
        flags.add(flag);
    }

    private void checkEmployerMismatch(ExtractionResult extraction, ClaudeLlmResponse llmData,
                                       ApplicationDataDto appData, List<DiscrepancyFlag> flags) {
        if (llmData.getEmployerName() == null || appData.getSelfReportedEmployer() == null) {
            return;
        }

        double score = similarity.apply(
                llmData.getEmployerName().toLowerCase(),
                appData.getSelfReportedEmployer().toLowerCase()
        );

        if (score < employerThreshold) {
            DiscrepancyFlag flag = new DiscrepancyFlag();
            flag.setExtractionResult(extraction);
            flag.setFieldName("employer");
            flag.setExtractedValue(llmData.getEmployerName());
            flag.setSelfReportedValue(appData.getSelfReportedEmployer());
            flag.setVariancePercentage(BigDecimal.valueOf((1 - score) * 100).setScale(4, RoundingMode.HALF_UP));
            flag.setSeverity(DiscrepancySeverity.HIGH);
            flag.setFlagType(DiscrepancyFlagType.EMPLOYER_MISMATCH);
            flags.add(flag);
        }
    }

    private void checkMissingFields(ExtractionResult extraction, ClaudeLlmResponse llmData,
                                    List<DiscrepancyFlag> flags) {
        Map<String, Object> fieldChecks = Map.of(
                "employer_name", (Object) (llmData.getEmployerName() != null ? llmData.getEmployerName() : ""),
                "gross_pay", (Object) (llmData.getGrossPay() != null ? llmData.getGrossPay() : ""),
                "net_pay", (Object) (llmData.getNetPay() != null ? llmData.getNetPay() : ""),
                "pay_frequency", (Object) (llmData.getPayFrequency() != null ? llmData.getPayFrequency() : "")
        );

        for (Map.Entry<String, Object> entry : fieldChecks.entrySet()) {
            if (entry.getValue().equals("")) {
                DiscrepancyFlag flag = new DiscrepancyFlag();
                flag.setExtractionResult(extraction);
                flag.setFieldName(entry.getKey());
                flag.setSeverity(DiscrepancySeverity.MEDIUM);
                flag.setFlagType(DiscrepancyFlagType.MISSING_FIELD);
                flags.add(flag);
            }
        }
    }
}
