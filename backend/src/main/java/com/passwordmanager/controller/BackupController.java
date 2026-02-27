package com.passwordmanager.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.passwordmanager.service.BackupService;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@Validated
public class BackupController {

    private final BackupService service;

    public BackupController(BackupService service) {
        this.service = service;
    }

    @GetMapping("/export")
    public String export() {
        return service.exportBackup();
    }

    @PostMapping("/restore")
    public Map<String, Object> restore(@RequestBody JsonNode req) {
        return service.restoreBackup(extractFileContent(req));
    }

    @PutMapping("/update")
    public Map<String, Object> updateBackup(@RequestBody JsonNode req) {
        return service.updateBackup(extractFileContent(req));
    }

    @PatchMapping("/validate")
    public Map<String, Object> validateBackup(@RequestBody JsonNode req) {
        return service.validateBackup(extractFileContent(req));
    }

    @DeleteMapping("/delete")
    public String deleteBackup() {
        return service.deleteBackup();
    }

    @GetMapping("/latest")
    public Map<String, Object> latestBackup() {
        return service.latestBackupInfo();
    }

    private String extractFileContent(JsonNode req) {
        if (req == null || req.isNull()) {
            throw new IllegalArgumentException("Backup content is required");
        }

        String content = null;
        if (req.isTextual()) {
            content = req.asText();
        } else if (req.has("fileContent")) {
            JsonNode fileContent = req.get("fileContent");
            if (fileContent != null && fileContent.isTextual()) {
                content = fileContent.asText();
            } else if (fileContent != null && fileContent.has("value") && fileContent.get("value").isTextual()) {
                content = fileContent.get("value").asText();
            }
        }

        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("Backup content is required");
        }

        if (content.length() > 2_000_000) {
            throw new IllegalArgumentException("Backup content is too large");
        }

        return content;
    }
}
