package com.passwordmanager.vault.security;

import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class EncryptionUtil {

    public String encrypt(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }

    public String decrypt(String encrypted) {
        return new String(Base64.getDecoder().decode(encrypted));
    }
}
