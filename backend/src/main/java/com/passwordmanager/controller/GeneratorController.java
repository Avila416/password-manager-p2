package com.passwordmanager.controller;


import lombok.extern.slf4j.Slf4j;
import com.passwordmanager.dto.*;
import com.passwordmanager.service.AuditService;
import com.passwordmanager.service.PasswordGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Validated
@Slf4j
public class GeneratorController {

    private final PasswordGeneratorService generatorService;
    private final AuditService auditService;

    // generate passwords
    @PostMapping("/generate")
    public List<PasswordResponse> generate(
            @Valid @RequestBody GeneratePasswordRequest req) {
        log.info("GeneratorController.generate called");
        return generatorService.generate(req);
    }

    // security audit
    @GetMapping("/audit")
    public AuditResponse audit() {
        log.info("GeneratorController.audit called");
        return auditService.generateAudit();
    }

    // list security alerts
    @GetMapping("/audit/alerts")
    public List<AlertResponse> alerts() {
        log.info("GeneratorController.alerts called");
        return auditService.getRecentAlerts();
    }

    // analyze each stored password
    @GetMapping("/audit/passwords-analysis")
    public List<StoredPasswordAnalysisResponse> passwordAnalysis() {
        log.info("GeneratorController.passwordAnalysis called");
        return auditService.analyzeStoredPasswords();
    }

    // clear old audit alerts/reports history
    @DeleteMapping("/audit/history")
    public String clearAuditHistory() {
        log.info("GeneratorController.clearAuditHistory called");
        return auditService.clearAuditHistory();
    }

}




