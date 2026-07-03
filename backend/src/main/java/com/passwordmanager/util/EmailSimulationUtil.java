package com.passwordmanager.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailSimulationUtil {
    public void sendOtpEmail(String email, String otp) {
        log.info("Simulated OTP email sent to {} with code {}", email, otp);
    }
}

