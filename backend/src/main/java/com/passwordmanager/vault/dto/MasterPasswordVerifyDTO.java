package com.passwordmanager.vault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MasterPasswordVerifyDTO {

    @NotBlank(message = "Master password is required")
    @Size(max = 256, message = "Master password cannot exceed 256 characters")
    private String masterPassword;

    public String getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }
}