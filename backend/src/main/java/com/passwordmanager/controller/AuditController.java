package com.passwordmanager.controller;

import com.passwordmanager.dto.AuditLogResponse;
import com.passwordmanager.service.AuditService;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
@Validated
public class AuditController {

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping
    public List<AuditLogResponse> logs(@RequestParam(required = false) @Size(max = 80, message = "Action filter is too long") String action,
                                       @RequestParam(required = false) @Size(max = 30, message = "Status filter is too long") String status,
                                       @RequestParam(required = false) @Size(max = 45, message = "IP filter is too long") String ip) {
        String normalizedAction = normalize(action);
        String normalizedStatus = normalize(status);
        String normalizedIp = normalize(ip);

        return service.getLogs().stream()
                .filter(log -> normalizedAction == null || normalize(log.getAction()).contains(normalizedAction))
                .filter(log -> normalizedStatus == null || normalize(log.getStatus()).contains(normalizedStatus))
                .filter(log -> normalizedIp == null || normalize(log.getIp()).contains(normalizedIp))
                .collect(Collectors.toList());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
