package com.passwordmanager.dto;

public class MasterPasswordSetupDTO {
    private String masterPassword;
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
