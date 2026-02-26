package com.passwordmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.passwordmanager.entity.VerificationCode;

public interface VerificationCodeRepository
        extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findTopByEmailOrderByExpiryTimeDesc(String email);
    void deleteByEmail(String email);
}
