package com.passwordmanager.vault.controller;

import com.passwordmanager.vault.dto.*;
import com.passwordmanager.vault.service.VaultService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vault")
@CrossOrigin(origins = "http://localhost:4200")
public class VaultController {

    private final VaultService service;

    public VaultController(VaultService service) {
        this.service = service;
    }

    // Create Entry
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PasswordEntryResponseDTO add(@RequestBody PasswordEntryRequestDTO dto) {
        return service.addEntry(dto);
    }

    // Update Entry
    @PutMapping("/{id}")
    public PasswordEntryResponseDTO update(@PathVariable Long id,
                                           @RequestBody UpdatePasswordEntryDTO dto) {
        return service.updateEntry(id, dto);
    }

    // Delete Entry (Master password as request param - safer)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestParam String masterPassword) {
        service.deleteEntry(id, masterPassword);
    }

    // Get All Entries
    @GetMapping
    public List<PasswordEntryResponseDTO> getAll() {
        return service.getAllEntries();
    }

    // Get Single Entry (verify using request param)
    @GetMapping("/{id}")
    public PasswordEntryResponseDTO getById(@PathVariable Long id,
                                            @RequestParam String masterPassword) {
        return service.getEntryById(id, masterPassword);
    }

    // Verify + Fetch (POST style)
    @PostMapping("/{id}/verify")
    public PasswordEntryResponseDTO verifyAndGet(@PathVariable Long id,
                                                 @RequestBody MasterPasswordVerifyDTO dto) {
        return service.getEntryById(id, dto.getMasterPassword());
    }

    // Mark as Favorite
    @PutMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markFavorite(@PathVariable Long id) {
        service.markFavorite(id);
    }

    // Get Favorites
    @GetMapping("/favorites")
    public List<PasswordEntryResponseDTO> getFavorites() {
        return service.getFavorites();
    }

    // Search + Filter + Sort
    @PostMapping("/search")
    public List<PasswordEntryResponseDTO> search(
            @RequestBody SearchFilterDTO dto,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {

        return service.searchAndFilter(dto, sortBy, direction);
    }
}