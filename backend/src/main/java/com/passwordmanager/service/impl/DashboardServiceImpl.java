package com.passwordmanager.service.impl;

import com.passwordmanager.dto.DashboardResponse;
import com.passwordmanager.exception.DashboardException;
import com.passwordmanager.repository.AuditLogRepository;
import com.passwordmanager.repository.PasswordEntryRepository;
import com.passwordmanager.security.EncryptionUtil;
import com.passwordmanager.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final AuditLogRepository auditLogRepository;
    private final EncryptionUtil encryptionUtil;

    public DashboardServiceImpl(PasswordEntryRepository passwordEntryRepository, AuditLogRepository auditLogRepository,
                                EncryptionUtil encryptionUtil) {
        this.passwordEntryRepository = passwordEntryRepository;
        this.auditLogRepository = auditLogRepository;
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    public DashboardResponse getDashboard() {
        try {
            long totalPasswords = passwordEntryRepository.count();
            long recentlyAdded = passwordEntryRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(7));
            long weakPasswords = passwordEntryRepository.findAll().stream()
                    .filter(entry -> isWeak(encryptionUtil.decrypt(entry.getEncryptedPassword())))
                    .count();
            Map<String, Long> actionStats = auditLogRepository.findAll().stream()
                    .collect(Collectors.groupingBy(a -> a.getAction(), Collectors.counting()));

            return new DashboardResponse(totalPasswords, weakPasswords, recentlyAdded, actionStats);
        } catch (Exception ex) {
            throw new DashboardException("Failed to fetch dashboard summary");
        }
    }

    private boolean isWeak(String password) {
        if (password == null || password.length() < 8) {
            return true;
        }
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasSymbol = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        return !(hasDigit && hasUpper && hasLower && hasSymbol);
    }
}
