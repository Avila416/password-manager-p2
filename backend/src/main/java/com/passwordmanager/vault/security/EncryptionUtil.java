package com.passwordmanager.vault.security;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    private final String secret;

    public EncryptionUtil(@Value("${vault.encryption.secret}") String secret) {
        this.secret = secret;
    }

    private SecretKeySpec getSecretKey() {
        byte[] keyBytes = secret.getBytes();
        byte[] key = new byte[16]; // 128-bit key
        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 16));
        return new SecretKeySpec(key, "AES");
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed");
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed");
        }
    }
}








































// package com.passwordmanager.vault.security;

// import org.springframework.stereotype.Component;

// import java.util.Base64;

// @Component
// public class EncryptionUtil {

//     public String encrypt(String raw) {
//         return Base64.getEncoder().encodeToString(raw.getBytes());
//     }

//     public String decrypt(String encrypted) {
//         return new String(Base64.getDecoder().decode(encrypted));
//     }
// }