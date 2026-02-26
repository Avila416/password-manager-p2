package com.passwordmanager.controller;

import com.passwordmanager.dto.PasswordEntryResponse;
import com.passwordmanager.service.VaultService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
@Validated
@CrossOrigin(originPatterns = {"http://localhost:*", "chrome-extension://*"})
public class VaultLookupController {

    private final VaultService vaultService;

    @GetMapping("/by-domain")
    public List<PasswordEntryResponse> byDomain(
            @RequestParam @NotBlank(message = "Domain is required") String domain) {
        return vaultService.getPasswordsByDomain(domain.trim());
    }
}
