package com.passwordmanager.controller;


import lombok.extern.slf4j.Slf4j;
import com.passwordmanager.security.EncryptionUtil;
import com.passwordmanager.security.MasterPasswordValidator;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(originPatterns = {"http://localhost:4200", "chrome-extension://*"})
@Slf4j
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
        log.info("SecurityController.encrypt called");
        return encryptionUtil.encrypt(body.get("plainText"));
    }

    @PostMapping("/decrypt")
    public String decrypt(@RequestBody Map<String, String> body) {
        log.info("SecurityController.decrypt called");
        return encryptionUtil.decrypt(body.get("encryptedText"));
    }

    @PostMapping("/verify-master")
    public boolean verifyMaster(@RequestBody Map<String, String> body) {
        log.info("SecurityController.verifyMaster called");
        return masterPasswordValidator.verify(body.get("masterPassword"));
    }
}


