package com.passwordmanager.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.passwordmanager.entity.VerificationCode;
import com.passwordmanager.exception.VerificationCodeExpiredException;
import com.passwordmanager.repository.VerificationCodeRepository;
import com.passwordmanager.util.EmailSimulationUtil;
import com.passwordmanager.util.OtpGenerator;

@Service
@Transactional
public class VerificationServiceImpl implements VerificationService {
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;

    private final VerificationCodeRepository verificationCodeRepository;
    private final OtpGenerator otpGenerator;
    private final EmailSimulationUtil emailSimulationUtil;

    public VerificationServiceImpl(
            VerificationCodeRepository verificationCodeRepository,
            OtpGenerator otpGenerator,
            EmailSimulationUtil emailSimulationUtil) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.otpGenerator = otpGenerator;
        this.emailSimulationUtil = emailSimulationUtil;
    }

    @Override
    public void generateCode(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase();
        verificationCodeRepository.deleteByEmail(normalizedEmail);

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(normalizedEmail);
        verificationCode.setCode(otpGenerator.generateOtp(CODE_LENGTH));
        verificationCode.setExpiryTime(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        verificationCodeRepository.save(verificationCode);

        emailSimulationUtil.sendOtpEmail(normalizedEmail, verificationCode.getCode());
    }

    @Override
    public boolean verifyCode(String email, String code) {
        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();
        VerificationCode savedCode = verificationCodeRepository
                .findTopByEmailOrderByExpiryTimeDesc(normalizedEmail)
                .orElse(null);

        if (savedCode == null) {
            return false;
        }

        if (savedCode.getExpiryTime().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.deleteByEmail(normalizedEmail);
            throw new VerificationCodeExpiredException("Verification code has expired");
        }

        boolean matches = savedCode.getCode().equals(code.trim());
        if (matches) {
            verificationCodeRepository.deleteByEmail(normalizedEmail);
        }
        return matches;
    }
}
