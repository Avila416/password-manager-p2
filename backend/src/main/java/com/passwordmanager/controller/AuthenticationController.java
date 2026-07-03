package com.passwordmanager.controller;


import lombok.extern.slf4j.Slf4j;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.passwordmanager.dto.AuthResponseDTO;
import com.passwordmanager.dto.ChangePasswordDTO;
import com.passwordmanager.dto.EmailRequestDTO;
import com.passwordmanager.dto.ForgotMasterPasswordRequestDTO;
import com.passwordmanager.dto.ForgotPasswordRequestDTO;
import com.passwordmanager.dto.LoginRequestDTO;
import com.passwordmanager.dto.MasterPasswordSetupDTO;
import com.passwordmanager.dto.OtpVerifyRequestDTO;
import com.passwordmanager.dto.RegisterRequestDTO;
import com.passwordmanager.dto.TwoFactorDTO;
import com.passwordmanager.dto.TwoFactorStatusDTO;
import com.passwordmanager.dto.UserProfileDTO;
import com.passwordmanager.entity.User;
import com.passwordmanager.exception.UnauthorizedAccessException;
import com.passwordmanager.repository.UserRepository;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.TokenBlacklistService;
import com.passwordmanager.service.TwoFactorService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/auth")
@Validated
@Slf4j
public class AuthenticationController {
    private final AuthService authService;
    private final TwoFactorService twoFactorService;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthenticationController(
            AuthService authService,
            TwoFactorService twoFactorService,
            UserRepository userRepository,
            TokenBlacklistService tokenBlacklistService) {
        this.authService = authService;
        this.twoFactorService = twoFactorService;
        this.userRepository = userRepository;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("AuthenticationController.register called");
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("AuthenticationController.login called");
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/password/forgot/request")
    public ResponseEntity<Map<String, String>> requestForgotPasswordCode(@Valid @RequestBody EmailRequestDTO request) {
        log.info("AuthenticationController.requestForgotPasswordCode called");
        authService.requestForgotPasswordCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Verification code sent"));
    }

    @PostMapping("/password/forgot/reset")
    public ResponseEntity<Map<String, String>> resetForgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("AuthenticationController.resetForgotPassword called");
        authService.resetForgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @PostMapping("/master-password/forgot/request")
    public ResponseEntity<Map<String, String>> requestForgotMasterPasswordCode(@Valid @RequestBody EmailRequestDTO request) {
        log.info("AuthenticationController.requestForgotMasterPasswordCode called");
        authService.requestForgotMasterPasswordCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Verification code sent"));
    }

    @PostMapping("/master-password/forgot/reset")
    public ResponseEntity<Map<String, String>> resetForgotMasterPassword(@Valid @RequestBody ForgotMasterPasswordRequestDTO request) {
        log.info("AuthenticationController.resetForgotMasterPassword called");
        authService.resetForgotMasterPassword(request);
        return ResponseEntity.ok(Map.of("message", "Master password reset successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("AuthenticationController.logout called");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedAccessException("Authorization header is required");
        }
        String token = authHeader.substring(7).trim();
        tokenBlacklistService.blacklistToken(token);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @GetMapping("/account")
    public ResponseEntity<UserProfileDTO> getAccount() {
        log.info("AuthenticationController.getAccount called");
        User user = getCurrentUser();
        UserProfileDTO profile = new UserProfileDTO();
        profile.setId(user.getId());
        profile.setName(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setTwoFactorEnabled(user.isTwoFactorEnabled());
        profile.setPhone(user.getPhone());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/master-password/setup")
    public ResponseEntity<Map<String, String>> setupMasterPassword(@Valid @RequestBody MasterPasswordSetupDTO request) {
        log.info("AuthenticationController.setupMasterPassword called");
        authService.setupMasterPassword(
                getCurrentUserEmail(),
                request.getMasterPassword(),
                request.getConfirmMasterPassword());
        return ResponseEntity.ok(Map.of("message", "Master password set successfully"));
    }

    @PutMapping("/master-password/change")
    public ResponseEntity<Map<String, String>> changeMasterPassword(@Valid @RequestBody ChangePasswordDTO request) {
        log.info("AuthenticationController.changeMasterPassword called");
        authService.changeMasterPassword(getCurrentUserEmail(), request);
        return ResponseEntity.ok(Map.of("message", "Master password changed successfully"));
    }

    @PutMapping("/2fa/status")
    public ResponseEntity<TwoFactorStatusDTO> updateTwoFactorStatus(@Valid @RequestBody TwoFactorStatusDTO request) {
        log.info("AuthenticationController.updateTwoFactorStatus called");
        return ResponseEntity.ok(authService.updateTwoFactorStatus(getCurrentUserEmail(), request));
    }

    @PostMapping("/2fa/request")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody TwoFactorDTO request) {
        log.info("AuthenticationController.requestOtp called");
        twoFactorService.requestOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerifyRequestDTO request) {
        log.info("AuthenticationController.verifyOtp called");
        boolean verified = twoFactorService.verifyOtp(request.getEmail(), request.getOtp());
        if (!verified) {
            throw new UnauthorizedAccessException("Invalid or expired OTP");
        }
        return ResponseEntity.ok(Map.of("message", "OTP verified", "verified", true));
    }

    private User getCurrentUser() {
        return userRepository.findByEmail(getCurrentUserEmail())
                .orElseThrow(() -> new UnauthorizedAccessException("Authenticated user not found"));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("Authentication required");
        }
        String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new UnauthorizedAccessException("Invalid authentication principal");
        }
        return email.trim().toLowerCase();
    }
}




