package com.passwordmanager.vault.service;

import com.passwordmanager.vault.dto.*;
import com.passwordmanager.vault.entity.PasswordEntry;
import com.passwordmanager.vault.repository.PasswordEntryRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VaultServiceImpl implements VaultService {

    private final PasswordEntryRepository repository;

    public VaultServiceImpl(PasswordEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public PasswordEntryResponseDTO addEntry(PasswordEntryRequestDTO dto) {

        PasswordEntry entry = new PasswordEntry();
        entry.setWebsite(dto.getWebsite());
        entry.setUsername(dto.getUsername());
        entry.setEncryptedPassword(dto.getPassword());
        entry.setFavorite(false);

        repository.save(entry);

        return mapToResponse(entry);
    }

    @Override
    public PasswordEntryResponseDTO updateEntry(Long id, UpdatePasswordEntryDTO dto) {

        PasswordEntry entry = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        entry.setWebsite(dto.getWebsite());
        entry.setUsername(dto.getUsername());
        entry.setEncryptedPassword(dto.getPassword());

        repository.save(entry);

        return mapToResponse(entry);
    }

    @Override
    public void deleteEntry(Long id, String masterPassword) {

        PasswordEntry entry = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        // TODO: Add master password verification logic here

        repository.delete(entry);
    }

    @Override
    public List<PasswordEntryResponseDTO> getAllEntries() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PasswordEntryResponseDTO getEntryById(Long id, String masterPassword) {

        PasswordEntry entry = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        // TODO: Verify master password here

        return mapToResponse(entry);
    }

    @Override
    public void markFavorite(Long id) {

        PasswordEntry entry = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        entry.setFavorite(true);
        repository.save(entry);
    }

    @Override
    public List<PasswordEntryResponseDTO> getFavorites() {

        return repository.findByFavoriteTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PasswordEntryResponseDTO> searchAndFilter(SearchFilterDTO dto,
                                                          String sortBy,
                                                          String direction) {

        // Simplified example — you can enhance later
        return repository.findAll()
                .stream()
                .filter(entry -> dto.getKeyword() == null ||
                        entry.getWebsite().contains(dto.getKeyword()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PasswordEntryResponseDTO mapToResponse(PasswordEntry entry) {

        PasswordEntryResponseDTO dto = new PasswordEntryResponseDTO();
        dto.setId(entry.getId());
        dto.setWebsite(entry.getWebsite());
        dto.setUsername(entry.getUsername());
        dto.setPassword(entry.getEncryptedPassword());
        dto.setFavorite(entry.isFavorite());

        return dto;
    }
}