package com.passwordmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordDTO {
    @NotBlank(message = "Old master password is required")
    @Size(min = 6, max = 128, message = "Old master password length must be between 6 and 128")
    private String oldMasterPassword;

    @NotBlank(message = "New master password is required")
    @Size(min = 6, max = 128, message = "New master password length must be between 6 and 128")
    private String newMasterPassword;

    public String getOldMasterPassword() {
        return oldMasterPassword;
    }

    public void setOldMasterPassword(String oldMasterPassword) {
        this.oldMasterPassword = oldMasterPassword;
    }

    public String getNewMasterPassword() {
        return newMasterPassword;
    }

    public void setNewMasterPassword(String newMasterPassword) {
        this.newMasterPassword = newMasterPassword;
    }
}
