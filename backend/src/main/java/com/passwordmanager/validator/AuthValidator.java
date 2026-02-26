package com.passwordmanager.validator;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.passwordmanager.dto.LoginRequestDTO;
import com.passwordmanager.dto.RegisterRequestDTO;
import com.passwordmanager.exception.ValidationException;

@Component
public class AuthValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public void validateRegister(RegisterRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Registration data is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Name is required");
        }
        validateEmail(request.getEmail());
        validatePassword(request.getPassword(), "Password must be at least 6 characters");
    }

    public void validateLogin(LoginRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Login data is required");
        }
        validateEmail(request.getEmail());
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ValidationException("Password is required");
        }
    }

    public void validateMasterPassword(String password) {
        validatePassword(password, "Master password must be at least 6 characters");
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Email format is invalid");
        }
    }

    private void validatePassword(String password, String message) {
        if (password == null || password.trim().length() < 6) {
            throw new ValidationException(message);
        }
    }
}
