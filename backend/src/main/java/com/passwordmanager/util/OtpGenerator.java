package com.passwordmanager.util;


import lombok.extern.slf4j.Slf4j;
import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OtpGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateOtp(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be greater than zero");
        }

        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}

