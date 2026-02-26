package com.passwordmanager.service.impl;

import com.passwordmanager.dto.AuditLogResponse;
import com.passwordmanager.entity.AuditLog;
import com.passwordmanager.exception.AuditException;
import com.passwordmanager.repository.AuditLogRepository;
import com.passwordmanager.service.AuditService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository repo;

    public AuditServiceImpl(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Override
    public void log(String action, String ip, String status) {

        if (isBlank(action) || isBlank(ip) || isBlank(status)) {
            throw new AuditException("Action, IP, and status are required");
        }

        try {
            AuditLog log = new AuditLog();
            log.setAction(action.trim());
            log.setIpAddress(ip.trim());
            log.setStatus(status.trim());
            log.setTimestamp(LocalDateTime.now());
            repo.save(log);
        } catch (Exception ex) {
            throw new AuditException("Failed to save audit log");
        }
    }

    @Override
    public List<AuditLogResponse> getLogs() {

        try {
            return repo.findAll()
                    .stream()
                    .sorted(Comparator.comparing(AuditLog::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()))
                            .reversed())
                    .map(a -> new AuditLogResponse(a.getAction(), a.getIpAddress(), a.getStatus(), a.getTimestamp()))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new AuditException("Failed to fetch audit logs");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
