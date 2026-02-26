package com.passwordmanager.service;

import com.passwordmanager.dto.PasswordEntryResponse;
import com.passwordmanager.dto.SavePasswordRequest;
import com.passwordmanager.entity.PasswordEntry;
import com.passwordmanager.exception.InvalidInputException;
import com.passwordmanager.repository.PasswordEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VaultService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final PasswordStrengthService strengthService;

    public PasswordEntryResponse saveGeneratedPassword(SavePasswordRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidInputException("Password is required");
        }

        LocalDateTime now = LocalDateTime.now();
        PasswordEntry saved = passwordEntryRepository.save(
                PasswordEntry.builder()
                        .username(request.getUsername())
                        .encryptedPassword(request.getPassword())
                        .createdAt(now)
                        .build()
        );

        return toResponse(saved);
    }

    public List<PasswordEntryResponse> getVaultPasswords() {
        return passwordEntryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PasswordEntryResponse> getPasswordsByDomain(String domain) {
        throw new InvalidInputException("Domain filter is not available in current vault model");
    }

    @Transactional
    public long clearGeneratedPasswords() {
        return passwordEntryRepository.deleteByUsername("generated-user");
    }

    private PasswordEntryResponse toResponse(PasswordEntry entry) {
        String password = entry.getEncryptedPassword() == null ? "" : entry.getEncryptedPassword();

        return PasswordEntryResponse.builder()
                .id(entry.getId())
                .username(entry.getUsername())
                .password(password)
                .strength(strengthService.checkStrength(password))
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
