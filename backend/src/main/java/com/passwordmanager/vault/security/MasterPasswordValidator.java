package com.passwordmanager.vault.security;

import org.springframework.stereotype.Component;

@Component
public class MasterPasswordValidator {

    // Demo system password
    private static final String SYSTEM_MASTER_PASSWORD = "admin";

    public boolean verify(String rawMasterPassword) {
        return SYSTEM_MASTER_PASSWORD.equals(rawMasterPassword);
    }
}