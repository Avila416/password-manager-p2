package com.passwordmanager.vault.service;

import java.util.List;

import com.passwordmanager.vault.dto.PasswordEntryRequestDTO;
import com.passwordmanager.vault.dto.PasswordEntryResponseDTO;
import com.passwordmanager.vault.dto.SearchFilterDTO;
import com.passwordmanager.vault.dto.UpdatePasswordEntryDTO;

public interface VaultService {

    PasswordEntryResponseDTO addEntry(PasswordEntryRequestDTO dto);

    PasswordEntryResponseDTO updateEntry(Long id, UpdatePasswordEntryDTO dto);

    void deleteEntry(Long id, String masterPassword);

    List<PasswordEntryResponseDTO> getAllEntries();

    PasswordEntryResponseDTO getEntryById(Long id, String masterPassword);

    void markFavorite(Long id);

    List<PasswordEntryResponseDTO> getFavorites();

    List<PasswordEntryResponseDTO> searchAndFilter(
            SearchFilterDTO dto,
            String sortBy,
            String direction);
}