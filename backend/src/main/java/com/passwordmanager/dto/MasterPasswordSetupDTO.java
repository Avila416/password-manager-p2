package com.passwordmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MasterPasswordSetupDTO {
    @NotBlank(message = "Master password is required")
    @Size(min = 6, max = 128, message = "Master password length must be between 6 and 128")
    private String masterPassword;

    @NotBlank(message = "Confirm master password is required")
    @Size(min = 6, max = 128, message = "Confirm master password length must be between 6 and 128")
    private String confirmMasterPassword;

    public String getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    public String getConfirmMasterPassword() {
        return confirmMasterPassword;
    }

    public void setConfirmMasterPassword(String confirmMasterPassword) {
        this.confirmMasterPassword = confirmMasterPassword;
    }
}
