package com.passwordmanager.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionUtil(@Value("${vault.encryption.key:${jwt.secret:dev-only-key-change-me}}") String keyMaterial) {
        this.keySpec = new SecretKeySpec(deriveAesKey(keyMaterial), "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return "";
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            String ivB64 = Base64.getEncoder().encodeToString(iv);
            String cipherB64 = Base64.getEncoder().encodeToString(encrypted);
            return "v1:" + ivB64 + ":" + cipherB64;
        } catch (Exception ex) {
            throw new IllegalStateException("Encryption failed", ex);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) {
            return "";
        }

        // New format: v1:<ivB64>:<cipherB64>
        if (encryptedText.startsWith("v1:")) {
            try {
                String[] parts = encryptedText.split(":");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid encrypted payload format");
                }

                byte[] iv = Base64.getDecoder().decode(parts[1]);
                byte[] cipherBytes = Base64.getDecoder().decode(parts[2]);

                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
                byte[] plain = cipher.doFinal(cipherBytes);
                return new String(plain, StandardCharsets.UTF_8);
            } catch (Exception ex) {
                // Keep API stable for legacy/mismatched-key rows instead of failing requests.
                return "";
            }
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            // Backward compatibility for old rows stored as plain text.
            return encryptedText;
        }
    }

    private byte[] deriveAesKey(String keyMaterial) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyMaterial.getBytes(StandardCharsets.UTF_8));
            byte[] key = new byte[16];
            System.arraycopy(hash, 0, key, 0, key.length);
            return key;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to derive encryption key", ex);
        }
    }
}
