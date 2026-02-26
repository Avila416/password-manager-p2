package com.passwordmanager.dto;

public class ForgotMasterPasswordRequestDTO {
    private String email;
    private String verificationCode;
    private String newMasterPassword;
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
