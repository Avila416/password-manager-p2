package com.passwordmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ForgotMasterPasswordRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Verification code is required")
    @Size(max = 20, message = "Verification code cannot exceed 20 characters")
    private String verificationCode;

    @NotBlank(message = "New master password is required")
    @Size(min = 6, max = 128, message = "New master password length must be between 6 and 128")
    private String newMasterPassword;

    @NotBlank(message = "Confirm master password is required")
    @Size(min = 6, max = 128, message = "Confirm master password length must be between 6 and 128")
    private String confirmMasterPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getNewMasterPassword() {
        return newMasterPassword;
    }

    public void setNewMasterPassword(String newMasterPassword) {
        this.newMasterPassword = newMasterPassword;
    }

    public String getConfirmMasterPassword() {
        return confirmMasterPassword;
    }

    public void setConfirmMasterPassword(String confirmMasterPassword) {
        this.confirmMasterPassword = confirmMasterPassword;
    }
}
