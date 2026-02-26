package com.passwordmanager.dto;

public class ChangePasswordDTO {
    private String oldMasterPassword;
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
