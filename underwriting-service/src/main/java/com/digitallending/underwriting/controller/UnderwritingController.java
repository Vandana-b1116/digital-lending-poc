package com.digitallending.underwriting.controller;

import com.digitallending.underwriting.dto.UnderwritingRequest;
import com.digitallending.underwriting.dto.UnderwritingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/underwriting")
@Slf4j
@CrossOrigin(origins = "*")
public class UnderwritingController {

    @Value("${underwriting.stub.decision:APPROVED}")
    private String stubDecision;

    @Value("${underwriting.stub.reason:Automated stub approval for POC demonstration}")
    private String stubReason;

    @PostMapping("/decisions")
    public UnderwritingResponse decide(@RequestBody UnderwritingRequest request) {
        log.info("Underwriting decision for application {}: {}", request.getApplicationId(), stubDecision);
        return new UnderwritingResponse(request.getApplicationId(), stubDecision, stubReason);
    }
}
