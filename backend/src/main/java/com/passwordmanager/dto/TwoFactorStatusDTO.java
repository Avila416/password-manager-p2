package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
@Slf4j
public class TwoFactorStatusDTO {
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

