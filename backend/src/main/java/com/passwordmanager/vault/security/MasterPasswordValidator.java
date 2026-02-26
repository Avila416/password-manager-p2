package com.passwordmanager.vault.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MasterPasswordValidator {

    private final String systemMasterPassword;

    public MasterPasswordValidator(@Value("${vault.master-password}") String systemMasterPassword) {
        this.systemMasterPassword = systemMasterPassword;
    }

    public boolean verify(String rawMasterPassword) {
        return systemMasterPassword.equals(rawMasterPassword);
    }
}
