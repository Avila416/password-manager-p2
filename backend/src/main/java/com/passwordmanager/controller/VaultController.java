package com.passwordmanager.controller;

import com.passwordmanager.dto.MasterPasswordVerifyDTO;
import com.passwordmanager.dto.PasswordEntryRequestDTO;
import com.passwordmanager.dto.PasswordEntryResponseDTO;
import com.passwordmanager.dto.SearchFilterDTO;
import com.passwordmanager.dto.UpdatePasswordEntryDTO;
import com.passwordmanager.service.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vault")
@CrossOrigin(originPatterns = {"http://localhost:4200", "chrome-extension://*"})
@Validated
@RequiredArgsConstructor
public class VaultController {

    private final VaultService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PasswordEntryResponseDTO add(@Valid @RequestBody PasswordEntryRequestDTO dto) {
        return service.addEntry(dto);
    }

    @PutMapping("/{id}")
    public PasswordEntryResponseDTO update(@PathVariable @Positive Long id,
                                           @Valid @RequestBody UpdatePasswordEntryDTO dto) {
        return service.updateEntry(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id,
                       @RequestParam @NotBlank(message = "Master password is required") String masterPassword) {
        service.deleteEntry(id, masterPassword.trim());
    }

    @GetMapping
    public List<PasswordEntryResponseDTO> getAll() {
        return service.getAllEntries();
    }

    @GetMapping("/{id}")
    public PasswordEntryResponseDTO getById(@PathVariable @Positive Long id,
                                            @RequestParam @NotBlank(message = "Master password is required") String masterPassword) {
        return service.getEntryById(id, masterPassword.trim());
    }

    @PostMapping("/{id}/verify")
    public PasswordEntryResponseDTO verifyAndGet(@PathVariable @Positive Long id,
                                                 @Valid @RequestBody MasterPasswordVerifyDTO dto) {
        return service.getEntryById(id, dto.getMasterPassword().trim());
    }

    @PutMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markFavorite(@PathVariable @Positive Long id) {
        service.markFavorite(id);
    }

    @GetMapping("/favorites")
    public List<PasswordEntryResponseDTO> getFavorites() {
        return service.getFavoritesEntries();
    }

    @GetMapping("/by-domain")
    public List<PasswordEntryResponseDTO> getByDomain(
            @RequestParam @NotBlank(message = "Domain is required") String domain) {
        return service.getEntriesByDomain(domain.trim());
    }

    @PostMapping("/search")
    public List<PasswordEntryResponseDTO> search(
            @Valid @RequestBody SearchFilterDTO dto,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false)
            @Pattern(regexp = "^(asc|desc)?$", message = "direction must be asc or desc") String direction) {

        return service.searchAndFilter(dto, sortBy, direction);
    }
}
