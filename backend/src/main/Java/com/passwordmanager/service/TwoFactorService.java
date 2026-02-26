package com.passwordmanager.service;

public interface TwoFactorService {
    void requestOtp(String email);
    boolean verifyOtp(String email, String otp);
}
