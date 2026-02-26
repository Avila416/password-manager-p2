package com.passwordmanager.controller;

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
public class GeneratorController {

    private final PasswordGeneratorService generatorService;
    private final AuditService auditService;

    // generate passwords
    @PostMapping("/generate")
    public List<PasswordResponse> generate(
            @Valid @RequestBody GeneratePasswordRequest req) {
        return generatorService.generate(req);
    }

    // security audit
    @GetMapping("/audit")
    public AuditResponse audit() {
        return auditService.generateAudit();
    }

    // list security alerts
    @GetMapping("/audit/alerts")
    public List<AlertResponse> alerts() {
        return auditService.getRecentAlerts();
    }

    // analyze each stored password
    @GetMapping("/audit/passwords-analysis")
    public List<StoredPasswordAnalysisResponse> passwordAnalysis() {
        return auditService.analyzeStoredPasswords();
    }

    // clear old audit alerts/reports history
    @DeleteMapping("/audit/history")
    public String clearAuditHistory() {
        return auditService.clearAuditHistory();
    }

}
