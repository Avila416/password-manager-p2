package com.passwordmanager.service;


import lombok.extern.slf4j.Slf4j;
import com.passwordmanager.dto.PasswordEntryRequestDTO;
import com.passwordmanager.dto.PasswordEntryResponseDTO;
import com.passwordmanager.dto.SearchFilterDTO;
import com.passwordmanager.dto.UpdatePasswordEntryDTO;
import com.passwordmanager.entity.PasswordEntry;
import com.passwordmanager.exception.BackupRequiredException;
import com.passwordmanager.exception.InvalidInputException;
import com.passwordmanager.exception.UnauthorizedAccessException;
import com.passwordmanager.repository.PasswordEntryRepository;
import com.passwordmanager.security.EncryptionUtil;
import com.passwordmanager.security.MasterPasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final PasswordStrengthService strengthService;
    private final MasterPasswordValidator masterPasswordValidator;
    private final EncryptionUtil encryptionUtil;
    private final BackupService backupService;

    public PasswordEntryResponseDTO addEntry(PasswordEntryRequestDTO dto) {
        log.info("VaultService.addEntry called");
        String encryptedPassword = encryptionUtil.encrypt(dto.getPassword());
        String normalizedTitle = normalizeTitle(dto.getTitle(), dto.getUsername());
        PasswordEntry saved = passwordEntryRepository.save(
                PasswordEntry.builder()
                        .title(normalizedTitle)
                        .username(dto.getUsername())
                        .encryptedPassword(encryptedPassword)
                        .website(dto.getWebsite() == null ? "" : dto.getWebsite().trim())
                        .category(dto.getCategory() == null || dto.getCategory().isBlank() ? "OTHER" : dto.getCategory().trim())
                        .favorite(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        return toVaultResponse(saved);
    }

    public List<PasswordEntryResponseDTO> getAllEntries() {
        log.info("VaultService.getAllEntries called");
        return passwordEntryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toVaultResponse)
                .toList();
    }

    @Transactional
    public PasswordEntryResponseDTO updateEntry(Long id, UpdatePasswordEntryDTO dto) {
        log.info("VaultService.updateEntry called");
        PasswordEntry entry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new InvalidInputException("Entry not found"));

        entry.setUsername(dto.getUsername());
        entry.setTitle(normalizeTitle(dto.getTitle(), dto.getUsername()));
        entry.setWebsite(dto.getWebsite() == null ? "" : dto.getWebsite().trim());
        entry.setCategory(dto.getCategory() == null || dto.getCategory().isBlank() ? "OTHER" : dto.getCategory().trim());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entry.setEncryptedPassword(encryptionUtil.encrypt(dto.getPassword()));
        }

        return toVaultResponse(passwordEntryRepository.save(entry));
    }

    @Transactional
    public void deleteEntry(Long id, String masterPassword) {
        log.info("VaultService.deleteEntry called");
        if (!masterPasswordValidator.verify(masterPassword)) {
            throw new UnauthorizedAccessException("Invalid master password");
        }
        if (!passwordEntryRepository.existsById(id)) {
            throw new InvalidInputException("Entry not found");
        }
        if (!backupService.hasValidBackup()) {
            throw new BackupRequiredException("A valid backup is required before deleting vault entries. Please export a backup first through Backup Operations.");
        }
        passwordEntryRepository.deleteById(id);
    }

    public PasswordEntryResponseDTO getEntryById(Long id, String masterPassword) {
        log.info("VaultService.getEntryById called");
        if (!masterPasswordValidator.verify(masterPassword)) {
            throw new UnauthorizedAccessException("Invalid master password");
        }
        PasswordEntry entry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new InvalidInputException("Entry not found"));
        return toVaultRevealResponse(entry);
    }

    @Transactional
    public void markFavorite(Long id) {
        log.info("VaultService.markFavorite called");
        PasswordEntry entry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new InvalidInputException("Entry not found"));
        entry.setFavorite(true);
        passwordEntryRepository.save(entry);
    }

    public List<PasswordEntryResponseDTO> getFavoritesEntries() {
        log.info("VaultService.getFavoritesEntries called");
        return passwordEntryRepository.findAllByFavoriteTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toVaultResponse)
                .toList();
    }

    public List<PasswordEntryResponseDTO> getEntriesByDomain(String domain) {
        log.info("VaultService.getEntriesByDomain called");
        String normalized = domain == null ? "" : domain.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return getAllEntries();
        }
        return getAllEntries().stream()
                .filter(entry -> containsIgnoreCase(entry.getWebsite(), normalized))
                .toList();
    }

    public List<PasswordEntryResponseDTO> searchAndFilter(SearchFilterDTO dto, String sortBy, String direction) {
        log.info("VaultService.searchAndFilter called");
        String keyword = dto == null || dto.getKeyword() == null
                ? ""
                : dto.getKeyword().trim().toLowerCase(Locale.ROOT);
        String category = dto == null || dto.getCategory() == null
                ? ""
                : dto.getCategory().trim().toUpperCase(Locale.ROOT);

        List<PasswordEntryResponseDTO> entries = getAllEntries().stream()
                .filter(entry -> keyword.isBlank()
                        || containsIgnoreCase(entry.getUsername(), keyword)
                        || containsIgnoreCase(entry.getTitle(), keyword)
                        || containsIgnoreCase(entry.getWebsite(), keyword))
                .filter(entry -> category.isBlank()
                        || "ALL".equals(category)
                        || "ALL CATEGORIES".equals(category)
                        || category.equalsIgnoreCase(entry.getCategory()))
                .toList();

        String sortKey = sortBy == null || sortBy.isBlank() ? "title" : sortBy;
        List<PasswordEntryResponseDTO> sorted = entries.stream()
                .sorted((a, b) -> compareBy(sortKey, a, b))
                .toList();

        if ("desc".equalsIgnoreCase(direction)) {
            List<PasswordEntryResponseDTO> desc = new ArrayList<>(sorted);
            Collections.reverse(desc);
            return desc;
        }
        return sorted;
    }

    @Transactional
    public long clearGeneratedPasswords() {
        log.info("VaultService.clearGeneratedPasswords called");
        return passwordEntryRepository.deleteByUsername("generated-user");
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private int compareBy(String sortBy, PasswordEntryResponseDTO a, PasswordEntryResponseDTO b) {
        if ("createdAt".equalsIgnoreCase(sortBy)) {
            LocalDateTime left = Objects.requireNonNullElse(a.getCreatedAt(), LocalDateTime.MIN);
            LocalDateTime right = Objects.requireNonNullElse(b.getCreatedAt(), LocalDateTime.MIN);
            return left.compareTo(right);
        }

        String left = Objects.requireNonNullElse(a.getTitle(), "");
        String right = Objects.requireNonNullElse(b.getTitle(), "");
        int byTitle = left.compareToIgnoreCase(right);
        if (byTitle != 0) {
            return byTitle;
        }

        String leftUser = Objects.requireNonNullElse(a.getUsername(), "");
        String rightUser = Objects.requireNonNullElse(b.getUsername(), "");
        return leftUser.compareToIgnoreCase(rightUser);
    }

    private PasswordEntryResponseDTO toVaultResponse(PasswordEntry entry) {
        String normalizedStoredPassword = normalizeStoredPassword(entry);
        String decryptedPassword = encryptionUtil.decrypt(normalizedStoredPassword);
        return PasswordEntryResponseDTO.builder()
                .id(entry.getId())
                .title(normalizeTitle(entry.getTitle(), entry.getUsername()))
                .username(entry.getUsername())
                .website(entry.getWebsite() == null ? "" : entry.getWebsite())
                .category(entry.getCategory() == null || entry.getCategory().isBlank() ? "OTHER" : entry.getCategory())
                .favorite(Boolean.TRUE.equals(entry.getFavorite()))
                .password(normalizedStoredPassword)
                .strength(strengthService.checkStrength(decryptedPassword))
                .createdAt(entry.getCreatedAt())
                .build();
    }

    private PasswordEntryResponseDTO toVaultRevealResponse(PasswordEntry entry) {
        String normalizedStoredPassword = normalizeStoredPassword(entry);
        String decryptedPassword = encryptionUtil.decrypt(normalizedStoredPassword);
        return PasswordEntryResponseDTO.builder()
                .id(entry.getId())
                .title(normalizeTitle(entry.getTitle(), entry.getUsername()))
                .username(entry.getUsername())
                .website(entry.getWebsite() == null ? "" : entry.getWebsite())
                .category(entry.getCategory() == null || entry.getCategory().isBlank() ? "OTHER" : entry.getCategory())
                .favorite(Boolean.TRUE.equals(entry.getFavorite()))
                .password(decryptedPassword)
                .strength(strengthService.checkStrength(decryptedPassword))
                .createdAt(entry.getCreatedAt())
                .build();
    }

    private String normalizeStoredPassword(PasswordEntry entry) {
        String stored = entry.getEncryptedPassword();
        if (stored == null || stored.isBlank()) {
            return "";
        }

        if (stored.startsWith("v1:")) {
            return stored;
        }

        // Legacy/plain values are migrated to encrypted format on read.
        String legacyPlain = encryptionUtil.decrypt(stored);
        if (legacyPlain == null || legacyPlain.isBlank()) {
            legacyPlain = stored;
        }

        String migrated = encryptionUtil.encrypt(legacyPlain);
        entry.setEncryptedPassword(migrated);
        passwordEntryRepository.save(entry);
        return migrated;
    }

    private String normalizeTitle(String title, String username) {
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }
        if (username != null && !username.trim().isEmpty()) {
            return username.trim();
        }
        return "";
    }
}


