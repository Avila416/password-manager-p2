package com.passwordmanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailSimulationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSimulationUtil.class);

    public void sendOtpEmail(String email, String otp) {
        LOGGER.info("Simulated OTP email sent to {} with code {}", email, otp);
    }
}
