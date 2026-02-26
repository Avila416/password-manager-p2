package com.passwordmanager.util;

import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class EncryptionUtil {

    public String encrypt(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public String decrypt(String data) {
        return new String(Base64.getDecoder().decode(data));
    }
}