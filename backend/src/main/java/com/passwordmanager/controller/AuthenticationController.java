package com.passwordmanager.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.passwordmanager.dto.ForgotMasterPasswordRequestDTO;
import com.passwordmanager.dto.ForgotPasswordRequestDTO;
import com.passwordmanager.dto.LoginRequestDTO;
import com.passwordmanager.dto.MasterPasswordSetupDTO;
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
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/password/forgot/request")
    public ResponseEntity<Map<String, String>> requestForgotPasswordCode(@RequestBody Map<String, String> request) {
        if (request == null || request.get("email") == null || request.get("email").isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        authService.requestForgotPasswordCode(request.get("email"));
        return ResponseEntity.ok(Map.of("message", "Verification code sent"));
    }

    @PostMapping("/password/forgot/reset")
    public ResponseEntity<Map<String, String>> resetForgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        authService.resetForgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @PostMapping("/master-password/forgot/request")
    public ResponseEntity<Map<String, String>> requestForgotMasterPasswordCode(@RequestBody Map<String, String> request) {
        if (request == null || request.get("email") == null || request.get("email").isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        authService.requestForgotMasterPasswordCode(request.get("email"));
        return ResponseEntity.ok(Map.of("message", "Verification code sent"));
    }

    @PostMapping("/master-password/forgot/reset")
    public ResponseEntity<Map<String, String>> resetForgotMasterPassword(@RequestBody ForgotMasterPasswordRequestDTO request) {
        authService.resetForgotMasterPassword(request);
        return ResponseEntity.ok(Map.of("message", "Master password reset successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
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
    public ResponseEntity<Map<String, String>> setupMasterPassword(@RequestBody MasterPasswordSetupDTO request) {
        authService.setupMasterPassword(
                getCurrentUserEmail(),
                request.getMasterPassword(),
                request.getConfirmMasterPassword());
        return ResponseEntity.ok(Map.of("message", "Master password set successfully"));
    }

    @PutMapping("/master-password/change")
    public ResponseEntity<Map<String, String>> changeMasterPassword(@RequestBody ChangePasswordDTO request) {
        authService.changeMasterPassword(getCurrentUserEmail(), request);
        return ResponseEntity.ok(Map.of("message", "Master password changed successfully"));
    }

    @PutMapping("/2fa/status")
    public ResponseEntity<TwoFactorStatusDTO> updateTwoFactorStatus(@RequestBody TwoFactorStatusDTO request) {
        return ResponseEntity.ok(authService.updateTwoFactorStatus(getCurrentUserEmail(), request));
    }

    @PostMapping("/2fa/request")
    public ResponseEntity<Map<String, String>> requestOtp(@RequestBody TwoFactorDTO request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        twoFactorService.requestOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody TwoFactorDTO request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
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
