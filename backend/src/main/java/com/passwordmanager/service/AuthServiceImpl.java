package com.passwordmanager.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.passwordmanager.dto.AuthResponseDTO;
import com.passwordmanager.dto.ChangePasswordDTO;
import com.passwordmanager.dto.ForgotMasterPasswordRequestDTO;
import com.passwordmanager.dto.ForgotPasswordRequestDTO;
import com.passwordmanager.dto.LoginRequestDTO;
import com.passwordmanager.dto.RegisterRequestDTO;
import com.passwordmanager.dto.TwoFactorStatusDTO;
import com.passwordmanager.entity.User;
import com.passwordmanager.exception.InvalidCredentialsException;
import com.passwordmanager.exception.ResourceNotFoundException;
import com.passwordmanager.exception.UnauthorizedAccessException;
import com.passwordmanager.exception.UserAlreadyExistsException;
import com.passwordmanager.exception.ValidationException;
import com.passwordmanager.repository.UserRepository;
import com.passwordmanager.security.JwtUtil;
import com.passwordmanager.validator.AuthValidator;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthValidator authValidator;
    private final TwoFactorService twoFactorService;
    private final VerificationService verificationService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthValidator authValidator,
            TwoFactorService twoFactorService,
            VerificationService verificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authValidator = authValidator;
        this.twoFactorService = twoFactorService;
        this.verificationService = verificationService;
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        authValidator.validateRegister(request);

        String normalizedEmail = normalizeEmail(request.getEmail());
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(request.getPhone() == null ? null : request.getPhone().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setMasterPassword(null);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponseDTO(token, "Registration successful");
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        authValidator.validateLogin(request);

        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword().trim(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (user.isTwoFactorEnabled()) {
            boolean verified = twoFactorService.verifyOtp(user.getEmail(), request.getOtp());
            if (!verified) {
                throw new UnauthorizedAccessException("2FA enabled. Valid OTP is required");
            }
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponseDTO(token, "Login successful");
    }

    @Override
    public void setupMasterPassword(String email, String masterPassword, String confirmMasterPassword) {
        User user = findUserByEmail(email);
        if (masterPassword == null || confirmMasterPassword == null) {
            throw new ValidationException("Master password and confirm password are required");
        }
        if (!masterPassword.trim().equals(confirmMasterPassword.trim())) {
            throw new ValidationException("Master password and confirm password must match");
        }
        authValidator.validateMasterPassword(masterPassword);
        user.setMasterPassword(passwordEncoder.encode(masterPassword.trim()));
        userRepository.save(user);
    }

    @Override
    public void requestForgotPasswordCode(String email) {
        User user = findUserByEmail(email);
        verificationService.generateCode(user.getEmail());
    }

    @Override
    public void resetForgotPassword(ForgotPasswordRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }

        String email = normalizeEmail(request.getEmail());
        String code = sanitize(request.getVerificationCode(), "Verification code is required");
        String newPassword = sanitize(request.getNewPassword(), "New password is required");
        String confirmPassword = sanitize(request.getConfirmPassword(), "Confirm password is required");

        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("New password and confirm password must match");
        }
        authValidator.validateMasterPassword(newPassword);

        boolean valid = verificationService.verifyCode(email, code);
        if (!valid) {
            throw new UnauthorizedAccessException("Invalid or expired verification code");
        }

        User user = findUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void requestForgotMasterPasswordCode(String email) {
        User user = findUserByEmail(email);
        verificationService.generateCode(user.getEmail());
    }

    @Override
    public void resetForgotMasterPassword(ForgotMasterPasswordRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }

        String email = normalizeEmail(request.getEmail());
        String code = sanitize(request.getVerificationCode(), "Verification code is required");
        String newMasterPassword = sanitize(request.getNewMasterPassword(), "New master password is required");
        String confirmMasterPassword = sanitize(request.getConfirmMasterPassword(), "Confirm master password is required");

        if (!newMasterPassword.equals(confirmMasterPassword)) {
            throw new ValidationException("New master password and confirm master password must match");
        }
        authValidator.validateMasterPassword(newMasterPassword);

        boolean valid = verificationService.verifyCode(email, code);
        if (!valid) {
            throw new UnauthorizedAccessException("Invalid or expired verification code");
        }

        User user = findUserByEmail(email);
        user.setMasterPassword(passwordEncoder.encode(newMasterPassword));
        userRepository.save(user);
    }

    @Override
    public void changeMasterPassword(String email, ChangePasswordDTO request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }
        User user = findUserByEmail(email);
        if (user.getMasterPassword() == null || user.getMasterPassword().isBlank()) {
            throw new ValidationException("Master password is not set. Please set it first.");
        }

        String oldMasterPassword = sanitize(request.getOldMasterPassword(), "Old master password is required");
        String newMasterPassword = sanitize(request.getNewMasterPassword(), "New master password is required");
        authValidator.validateMasterPassword(newMasterPassword);

        if (!passwordEncoder.matches(oldMasterPassword, user.getMasterPassword())) {
            throw new InvalidCredentialsException("Old master password is incorrect");
        }
        if (oldMasterPassword.equals(newMasterPassword)) {
            throw new ValidationException("New master password must be different");
        }

        user.setMasterPassword(passwordEncoder.encode(newMasterPassword));
        userRepository.save(user);
    }

    @Override
    public TwoFactorStatusDTO updateTwoFactorStatus(String email, TwoFactorStatusDTO request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }
        User user = findUserByEmail(email);
        user.setTwoFactorEnabled(request.isEnabled());
        userRepository.save(user);

        TwoFactorStatusDTO response = new TwoFactorStatusDTO();
        response.setEnabled(user.isTwoFactorEnabled());
        return response;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalizeEmail(String email) {
        String normalized = sanitize(email, "Email is required").toLowerCase(Locale.ROOT);
        if (!normalized.contains("@")) {
            throw new ValidationException("Email format is invalid");
        }
        return normalized;
    }

    private String sanitize(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }
}
