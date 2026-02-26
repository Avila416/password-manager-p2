package com.passwordmanager.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.passwordmanager.util.EmailSimulationUtil;
import com.passwordmanager.util.OtpGenerator;

@Service
public class TwoFactorServiceImpl implements TwoFactorService {
    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_SECONDS = 300;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final OtpGenerator otpGenerator;
    private final EmailSimulationUtil emailSimulationUtil;
    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();

    public TwoFactorServiceImpl(OtpGenerator otpGenerator, EmailSimulationUtil emailSimulationUtil) {
        this.otpGenerator = otpGenerator;
        this.emailSimulationUtil = emailSimulationUtil;
    }

    @Override
    public void requestOtp(String email) {
        validateEmail(email);

        String otp = otpGenerator.generateOtp(OTP_LENGTH);
        Instant expiresAt = Instant.now().plusSeconds(OTP_TTL_SECONDS);
        otpStore.put(normalize(email), new OtpRecord(otp, expiresAt));
        emailSimulationUtil.sendOtpEmail(email, otp);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        validateEmail(email);
        if (otp == null || otp.isBlank()) {
            return false;
        }

        String key = normalize(email);
        OtpRecord stored = otpStore.get(key);
        if (stored == null) {
            return false;
        }
        if (Instant.now().isAfter(stored.getExpiresAt())) {
            otpStore.remove(key);
            return false;
        }
        boolean matches = stored.getOtp().equals(otp.trim());
        if (matches) {
            otpStore.remove(key);
        }
        return matches;
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Email format is invalid");
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private static final class OtpRecord {
        private final String otp;
        private final Instant expiresAt;

        private OtpRecord(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }

        private String getOtp() {
            return otp;
        }

        private Instant getExpiresAt() {
            return expiresAt;
        }
    }
}
