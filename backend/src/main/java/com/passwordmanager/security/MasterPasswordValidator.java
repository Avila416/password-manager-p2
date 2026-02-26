package com.passwordmanager.security;

import com.passwordmanager.entity.User;
import com.passwordmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MasterPasswordValidator {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean verify(String masterPassword) {
        if (masterPassword == null || masterPassword.isBlank()) {
            return false;
        }

        String candidate = masterPassword.trim();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null && user.getMasterPassword() != null && !user.getMasterPassword().isBlank()) {
                return passwordEncoder.matches(candidate, user.getMasterPassword());
            }
        }

        // Fallback when security context/token is unavailable: accept if it matches
        // any configured master password.
        return userRepository.findAll().stream()
                .map(User::getMasterPassword)
                .filter(hash -> hash != null && !hash.isBlank())
                .anyMatch(hash -> passwordEncoder.matches(candidate, hash));
    }
}
