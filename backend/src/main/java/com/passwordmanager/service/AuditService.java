package com.passwordmanager.service;

import com.passwordmanager.dto.AlertResponse;
import com.passwordmanager.dto.AuditLogResponse;
import com.passwordmanager.dto.AuditResponse;
import com.passwordmanager.dto.StoredPasswordAnalysisResponse;
import com.passwordmanager.entity.AuditLog;
import com.passwordmanager.entity.AuditReport;
import com.passwordmanager.entity.PasswordEntry;
import com.passwordmanager.entity.SecurityAlert;
import com.passwordmanager.exception.AuditException;
import com.passwordmanager.exception.OperationFailedException;
import com.passwordmanager.repository.AuditLogRepository;
import com.passwordmanager.repository.AuditReportRepository;
import com.passwordmanager.repository.PasswordEntryRepository;
import com.passwordmanager.repository.SecurityAlertRepository;
import com.passwordmanager.security.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final PasswordEntryRepository repo;
    private final SecurityAlertRepository alertRepo;
    private final AuditReportRepository auditReportRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordStrengthService strengthService;
    private final EncryptionUtil encryptionUtil;

    public void log(String action, String ip, String status) {
        if (isBlank(action) || isBlank(ip) || isBlank(status)) {
            throw new AuditException("Action, IP, and status are required");
        }

        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action.trim());
            auditLog.setIpAddress(ip.trim());
            auditLog.setStatus(status.trim());
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            throw new AuditException("Failed to save audit log");
        }
    }

    public List<AuditLogResponse> getLogs() {
        try {
            return auditLogRepository.findAll()
                    .stream()
                    .sorted(Comparator.comparing(AuditLog::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()))
                            .reversed())
                    .map(a -> new AuditLogResponse(a.getAction(), a.getIpAddress(), a.getStatus(), a.getTimestamp()))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new AuditException("Failed to fetch audit logs");
        }
    }

    public AuditResponse generateAudit() {
        List<PasswordEntry> list = repo.findAllByOrderByCreatedAtDesc();

        if (list.isEmpty()) {
            LocalDateTime generatedAt = LocalDateTime.now();
            AuditReport savedReport = auditReportRepository.save(
                    AuditReport.builder()
                            .totalPasswords(0)
                            .weakPasswords(0)
                            .reusedPasswords(0)
                            .oldPasswords(0)
                            .generatedAt(generatedAt)
                            .build()
            );

            return AuditResponse.builder()
                    .total(0)
                    .weak(0)
                    .reused(0)
                    .old(0)
                    .reportId(savedReport.getId())
                    .alertCount(0)
                    .generatedAt(generatedAt)
                    .build();
        }

        int weak = 0;
        int reused = 0;
        int old = 0;
        int alertsCreated = 0;
        Map<String, Integer> passwordUseMap = new HashMap<>();
        Map<String, List<PasswordEntry>> passwordEntriesMap = new HashMap<>();

        try {
            for (PasswordEntry entry : list) {
                String password = passwordOf(entry);
                String strength = strengthService.checkStrength(password);

                if ("WEAK".equalsIgnoreCase(strength)) {
                    weak++;
                    if (saveAlert("Weak password detected for: " + safeLabel(entry), "WEAK", "HIGH")) {
                        alertsCreated++;
                    }
                }

                if (!password.isBlank()) {
                    passwordUseMap.put(password, passwordUseMap.getOrDefault(password, 0) + 1);
                    passwordEntriesMap.computeIfAbsent(password, k -> new ArrayList<>()).add(entry);
                }

                if (entry.getCreatedAt() != null &&
                        entry.getCreatedAt().isBefore(LocalDateTime.now().minusDays(90))) {
                    old++;
                    if (saveAlert("Old password detected for: " + safeLabel(entry), "OLD", "MEDIUM")) {
                        alertsCreated++;
                    }
                }
            }

            for (Map.Entry<String, Integer> mapEntry : passwordUseMap.entrySet()) {
                int count = mapEntry.getValue();
                if (count > 1) {
                    reused += count;
                    String usageDetails = usageDetails(passwordEntriesMap.getOrDefault(mapEntry.getKey(), List.of()));
                    String message = "Reused password detected (" + count + " occurrences)";
                    if (!usageDetails.isBlank()) {
                        message += ". Used by: " + usageDetails;
                    }
                    if (saveAlert(message, "REUSED", "HIGH")) {
                        alertsCreated++;
                    }
                }
            }
        } catch (Exception ex) {
            throw new OperationFailedException("Audit generation failed");
        }

        LocalDateTime generatedAt = LocalDateTime.now();
        AuditReport savedReport = auditReportRepository.save(
                AuditReport.builder()
                        .totalPasswords(list.size())
                        .weakPasswords(weak)
                        .reusedPasswords(reused)
                        .oldPasswords(old)
                        .generatedAt(generatedAt)
                        .build()
        );

        return AuditResponse.builder()
                .total(list.size())
                .weak(weak)
                .reused(reused)
                .old(old)
                .reportId(savedReport.getId())
                .alertCount(alertsCreated)
                .generatedAt(generatedAt)
                .build();
    }

    public List<AlertResponse> getRecentAlerts() {
        return alertRepo.findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toAlertResponse)
                .toList();
    }

    public List<StoredPasswordAnalysisResponse> analyzeStoredPasswords() {
        List<PasswordEntry> list = repo.findAllByOrderByCreatedAtDesc();
        Map<String, Integer> useMap = new HashMap<>();

        for (PasswordEntry entry : list) {
            String password = passwordOf(entry);
            if (!password.isBlank()) {
                useMap.put(password, useMap.getOrDefault(password, 0) + 1);
            }
        }

        return list.stream()
                .map(entry -> toAnalysisResponse(entry, useMap))
                .toList();
    }

    public String clearAuditHistory() {
        long alertCount = alertRepo.count();
        long reportCount = auditReportRepository.count();

        alertRepo.deleteAllInBatch();
        auditReportRepository.deleteAllInBatch();

        return "Deleted audit history. Alerts: " + alertCount + ", Reports: " + reportCount;
    }

    private StoredPasswordAnalysisResponse toAnalysisResponse(PasswordEntry entry, Map<String, Integer> useMap) {
        String password = passwordOf(entry);
        String strength = strengthService.checkStrength(password);
        boolean isOld = entry.getCreatedAt() != null &&
                entry.getCreatedAt().isBefore(LocalDateTime.now().minusDays(90));
        boolean isReused = !password.isBlank() && useMap.getOrDefault(password, 0) > 1;
        boolean isWeak = "WEAK".equalsIgnoreCase(strength);

        return StoredPasswordAnalysisResponse.builder()
                .entryId(entry.getId())
                .username(entry.getUsername())
                .website(entry.getWebsite())
                .strength(strength)
                .weak(isWeak)
                .reused(isReused)
                .old(isOld)
                .createdAt(entry.getCreatedAt())
                .build();
    }

    private AlertResponse toAlertResponse(SecurityAlert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .message(alert.getMessage())
                .severity(alert.getSeverity())
                .type(alert.getType())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    private String passwordOf(PasswordEntry entry) {
        if (entry.getEncryptedPassword() == null || entry.getEncryptedPassword().isBlank()) {
            return "";
        }
        return encryptionUtil.decrypt(entry.getEncryptedPassword());
    }

    private String safeLabel(PasswordEntry entry) {
        String username = entry.getUsername() == null ? "" : entry.getUsername().trim();
        String website = entry.getWebsite() == null ? "" : entry.getWebsite().trim();

        if (!username.isBlank() && !website.isBlank()) {
            return username + " @ " + website;
        }
        if (!username.isBlank()) {
            return username;
        }
        if (!website.isBlank()) {
            return website;
        }
        return "entry#" + entry.getId();
    }

    private String usageDetails(List<PasswordEntry> entries) {
        Set<String> labels = new LinkedHashSet<>();
        for (PasswordEntry entry : entries) {
            labels.add(safeLabel(entry));
        }

        if (labels.isEmpty()) {
            return "";
        }

        List<String> list = new ArrayList<>(labels);
        int limit = Math.min(5, list.size());
        String joined = String.join(", ", list.subList(0, limit));
        if (list.size() > limit) {
            joined += " +" + (list.size() - limit) + " more";
        }
        return joined;
    }

    private boolean saveAlert(String msg, String type, String severity) {
        LocalDateTime now = LocalDateTime.now();
        boolean duplicateRecent = alertRepo.existsByMessageAndTypeAndCreatedAtAfter(
                msg,
                type,
                now.minusHours(24)
        );
        if (duplicateRecent) {
            return false;
        }

        alertRepo.save(
                SecurityAlert.builder()
                        .message(msg)
                        .type(type)
                        .severity(severity)
                        .createdAt(now)
                        .build()
        );
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

