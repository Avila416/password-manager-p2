package com.passwordmanager.vault.controller;

import org.springframework.web.bind.annotation.*;
import com.passwordmanager.vault.security.EncryptionUtil;
import com.passwordmanager.vault.security.MasterPasswordValidator;


import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final EncryptionUtil encryptionUtil;

    private final MasterPasswordValidator masterPasswordValidator;

    public SecurityController(
            EncryptionUtil encryptionUtil,
            MasterPasswordValidator masterPasswordValidator
    ) {
        this.encryptionUtil = encryptionUtil;
        this.masterPasswordValidator = masterPasswordValidator;
    }

    @PostMapping("/encrypt")
    public String encrypt(@RequestBody Map<String, String> body) {
        return encryptionUtil.encrypt(body.get("plainText"));
    }

    @PostMapping("/decrypt")
    public String decrypt(@RequestBody Map<String, String> body) {
        return encryptionUtil.decrypt(body.get("encryptedText"));
    }

    @PostMapping("/verify-master")
    public boolean verifyMaster(@RequestBody Map<String, String> body) {
        return masterPasswordValidator.verify(body.get("masterPassword"));
    }
}
