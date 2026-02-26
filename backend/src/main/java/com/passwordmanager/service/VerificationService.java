package com.passwordmanager.service;

public interface VerificationService {
    void generateCode(String email);
    boolean verifyCode(String email, String code);
}
