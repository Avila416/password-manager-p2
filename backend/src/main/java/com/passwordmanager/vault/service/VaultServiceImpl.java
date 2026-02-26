package com.passwordmanager.vault.service;

import com.passwordmanager.vault.dto.PasswordEntryRequestDTO;
import com.passwordmanager.vault.dto.PasswordEntryResponseDTO;
import com.passwordmanager.vault.dto.SearchFilterDTO;
import com.passwordmanager.vault.dto.UpdatePasswordEntryDTO;
import com.passwordmanager.vault.entity.PasswordEntry;
import com.passwordmanager.vault.exception.OperationFailedException;
import com.passwordmanager.vault.exception.ResourceNotFoundException;
import com.passwordmanager.vault.repository.PasswordEntryRepository;
import com.passwordmanager.vault.security.EncryptionUtil;
import com.passwordmanager.vault.security.MasterPasswordValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VaultServiceImpl implements VaultService {

    private final PasswordEntryRepository repository;
    private final EncryptionUtil encryptionUtil;
    private final MasterPasswordValidator masterPasswordValidator;

    public VaultServiceImpl(
            PasswordEntryRepository repository,
            EncryptionUtil encryptionUtil,
            MasterPasswordValidator masterPasswordValidator
    ) {
        this.repository = repository;
        this.encryptionUtil = encryptionUtil;
        this.masterPasswordValidator = masterPasswordValidator;
    }

    @Override
    @Transactional
    public PasswordEntryResponseDTO addEntry(PasswordEntryRequestDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new OperationFailedException("Password is required");
        }

        PasswordEntry entry = new PasswordEntry();
        entry.setWebsite(dto.getWebsite());
        entry.setUsername(dto.getUsername());
        entry.setEncryptedPassword(encryptionUtil.encrypt(dto.getPassword()));
        entry.setFavorite(false);
        entry.setTitle(dto.getTitle());
        entry.setCategory(dto.getCategory());
        entry.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());

        repository.save(entry);
        return mapToResponse(entry, false);
    }

    @Override
    @Transactional
    public PasswordEntryResponseDTO updateEntry(Long id, UpdatePasswordEntryDTO dto) {
        PasswordEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found: " + id));

        if (dto.getWebsite() != null) {
            entry.setWebsite(dto.getWebsite());
        }
        if (dto.getUsername() != null) {
            entry.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null) {
            entry.setEncryptedPassword(encryptionUtil.encrypt(dto.getPassword()));
        }
        if (dto.getTitle() != null) {
            entry.setTitle(dto.getTitle());
        }
        if (dto.getCategory() != null) {
            entry.setCategory(dto.getCategory());
        }
        if (dto.getFavorite() != null) {
            entry.setFavorite(dto.getFavorite());
        }
        entry.setUpdatedAt(LocalDateTime.now());

        repository.save(entry);
        return mapToResponse(entry, false);
    }

    @Override
    @Transactional
    public void deleteEntry(Long id, String masterPassword) {
        PasswordEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found: " + id));

        verifyMasterPasswordOrThrow(masterPassword);
        repository.delete(entry);
    }

    @Override
    public List<PasswordEntryResponseDTO> getAllEntries() {
        return repository.findAll()
                .stream()
                .map(entry -> mapToResponse(entry, false))
                .collect(Collectors.toList());
    }

    @Override
    public PasswordEntryResponseDTO getEntryById(Long id, String masterPassword) {
        PasswordEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found: " + id));

        verifyMasterPasswordOrThrow(masterPassword);
        return mapToResponse(entry, true);
    }

    @Override
    @Transactional
    public void markFavorite(Long id) {
        PasswordEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found: " + id));

        entry.setFavorite(true);
        entry.setUpdatedAt(LocalDateTime.now());
        repository.save(entry);
    }

    @Override
    public List<PasswordEntryResponseDTO> getFavorites() {
        return repository.findByFavoriteTrue()
                .stream()
                .map(entry -> mapToResponse(entry, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<PasswordEntryResponseDTO> getEntriesByDomain(String domain) {
        String normalizedDomain = normalizeDomain(domain);
        return repository.findAll()
                .stream()
                .filter(entry -> matchesDomain(entry.getWebsite(), normalizedDomain))
                .map(entry -> mapToResponse(entry, true))
                .collect(Collectors.toList());
    }

    @Override
    public List<PasswordEntryResponseDTO> searchAndFilter(SearchFilterDTO dto, String sortBy, String direction) {
        String finalSortBy = sortBy != null ? sortBy : dto.getSortBy();
        Comparator<PasswordEntry> comparator = buildComparator(finalSortBy, direction);

        return repository.findAll()
                .stream()
                .filter(entry -> matchesKeyword(entry, dto.getKeyword()))
                .filter(entry -> dto.getCategory() == null || dto.getCategory() == entry.getCategory())
                .sorted(comparator)
                .map(entry -> mapToResponse(entry, false))
                .collect(Collectors.toList());
    }

    private PasswordEntryResponseDTO mapToResponse(PasswordEntry entry, boolean includeDecryptedPassword) {
        PasswordEntryResponseDTO dto = new PasswordEntryResponseDTO();
        dto.setId(entry.getId());
        dto.setTitle(entry.getTitle());
        dto.setWebsite(entry.getWebsite());
        dto.setUsername(entry.getUsername());
        dto.setPassword(resolvePasswordForResponse(entry, includeDecryptedPassword));
        dto.setCategory(entry.getCategory());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setFavorite(entry.isFavorite());
        return dto;
    }

    private String resolvePasswordForResponse(PasswordEntry entry, boolean includeDecryptedPassword) {
        if (!includeDecryptedPassword) {
            return entry.getEncryptedPassword();
        }
        try {
            return encryptionUtil.decrypt(entry.getEncryptedPassword());
        } catch (Exception ex) {
            throw new OperationFailedException("Stored password cannot be decrypted for entry: " + entry.getId());
        }
    }

    private void verifyMasterPasswordOrThrow(String masterPassword) {
        if (!masterPasswordValidator.verify(masterPassword)) {
            throw new OperationFailedException("Invalid master password");
        }
    }

    private boolean matchesKeyword(PasswordEntry entry, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.toLowerCase();
        return containsIgnoreCase(entry.getTitle(), normalized)
                || containsIgnoreCase(entry.getUsername(), normalized)
                || containsIgnoreCase(entry.getWebsite(), normalized);
    }

    private boolean containsIgnoreCase(String value, String keywordLowerCase) {
        return value != null && value.toLowerCase().contains(keywordLowerCase);
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
        if (rawDomain == null || rawDomain.isBlank()) {
            throw new OperationFailedException("Domain is required");
        }
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

    private Comparator<PasswordEntry> buildComparator(String sortBy, String direction) {
        Comparator<PasswordEntry> comparator;
        if ("date".equalsIgnoreCase(sortBy) || "createdAt".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(
                    PasswordEntry::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
        } else {
            comparator = Comparator.comparing(
                    PasswordEntry::getTitle,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
        }
        if ("desc".equalsIgnoreCase(direction)) {
            return comparator.reversed();
        }
        return comparator;
    }
}
