package com.passwordmanager.controller;

import com.passwordmanager.dto.VaultEntryRequest;
import com.passwordmanager.entity.VaultEntry;
import com.passwordmanager.exception.ResourceNotFoundException;
import com.passwordmanager.repository.VaultEntryRepository;
import com.passwordmanager.service.AuditService;
import com.passwordmanager.util.AuditActions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vault")
@CrossOrigin(originPatterns = {"http://localhost:*"})
@Validated
public class VaultController {

    private final VaultEntryRepository vaultEntryRepository;
    private final AuditService auditService;

    public VaultController(VaultEntryRepository vaultEntryRepository, AuditService auditService) {
        this.vaultEntryRepository = vaultEntryRepository;
        this.auditService = auditService;
    }

    @GetMapping("/entries")
    public List<VaultEntry> allEntries() {
        return vaultEntryRepository.findAll();
    }

    @PostMapping("/entries")
    public VaultEntry addEntry(@Valid @RequestBody VaultEntryRequest request) {
        VaultEntry entry = new VaultEntry();
        entry.setTitle(request.getTitle());
        entry.setUsername(request.getUsername());
        entry.setPassword(request.getPassword());
        entry.setWebsite(request.getWebsite() == null || request.getWebsite().isBlank() ? request.getTitle() : request.getWebsite());
        entry.setCreatedAt(LocalDateTime.now());
        return vaultEntryRepository.save(entry);
    }

    @GetMapping("/by-domain")
    public List<VaultEntry> byDomain(@RequestParam @NotBlank(message = "Domain is required") String domain) {
        String normalizedDomain = normalizeDomain(domain);
        return vaultEntryRepository.findAll()
                .stream()
                .filter(entry -> {
                    String source = entry.getWebsite() == null || entry.getWebsite().isBlank() ? entry.getTitle() : entry.getWebsite();
                    return matchesDomain(source, normalizedDomain);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/entries/{id}")
    public VaultEntry viewEntry(@PathVariable @Positive(message = "ID must be positive") Long id,
                                @RequestParam(defaultValue = "127.0.0.1") @NotBlank(message = "IP must not be blank") String ip) {
        VaultEntry entry = vaultEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vault entry not found: " + id));
        auditService.log(AuditActions.PASSWORD_VIEW, ip, "SUCCESS");
        return entry;
    }

    @DeleteMapping("/entries/{id}")
    public String deleteEntry(@PathVariable @Positive(message = "ID must be positive") Long id,
                              @RequestParam(defaultValue = "127.0.0.1") @NotBlank(message = "IP must not be blank") String ip) {
        if (!vaultEntryRepository.existsById(id)) {
            auditService.log(AuditActions.DELETE_ENTRY, ip, "FAILED");
            throw new ResourceNotFoundException("Vault entry not found: " + id);
        }
        vaultEntryRepository.deleteById(id);
        auditService.log(AuditActions.DELETE_ENTRY, ip, "SUCCESS");
        return "Vault entry deleted";
    }

    @PostMapping("/monitor/login")
    public String logLoginAttempt(@RequestParam boolean success,
                                  @RequestParam(defaultValue = "127.0.0.1") @NotBlank(message = "IP must not be blank") String ip) {
        if (success) {
            auditService.log(AuditActions.LOGIN_ATTEMPT, ip, "SUCCESS");
            return "Login attempt logged";
        }
        auditService.log(AuditActions.FAILED_LOGIN_ATTEMPT, ip, "FAILED");
        return "Failed login attempt logged";
    }

    @PostMapping("/monitor/master-password-change")
    public String logMasterPasswordChange(@RequestParam(defaultValue = "127.0.0.1") @NotBlank(message = "IP must not be blank") String ip) {
        auditService.log(AuditActions.MASTER_PASSWORD_CHANGE, ip, "SUCCESS");
        return "Master password change logged";
    }

    private boolean matchesDomain(String website, String normalizedDomain) {
        if (website == null || website.isBlank()) {
            return false;
        }
        String siteDomain = extractDomain(website);
        return siteDomain.equals(normalizedDomain)
                || siteDomain.endsWith("." + normalizedDomain)
                || normalizedDomain.endsWith("." + siteDomain);
    }

    private String normalizeDomain(String rawDomain) {
        String trimmed = rawDomain.trim().toLowerCase(Locale.ROOT);
        if (trimmed.startsWith("www.")) {
            return trimmed.substring(4);
        }
        return trimmed;
    }

    private String extractDomain(String website) {
        String candidate = website.trim();
        if (!candidate.startsWith("http://") && !candidate.startsWith("https://")) {
            candidate = "https://" + candidate;
        }
        try {
            String host = Optional.ofNullable(URI.create(candidate).getHost()).orElse("");
            if (host.startsWith("www.")) {
                return host.substring(4).toLowerCase(Locale.ROOT);
            }
            return host.toLowerCase(Locale.ROOT);
        } catch (Exception ex) {
            return website.trim().toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
        }
    }
}
