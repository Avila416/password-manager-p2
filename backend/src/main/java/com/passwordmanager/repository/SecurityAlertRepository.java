package com.passwordmanager.repository;

import com.passwordmanager.entity.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {

    List<SecurityAlert> findTop100ByOrderByCreatedAtDesc();

    boolean existsByMessageAndTypeAndCreatedAtAfter(String message, String type, LocalDateTime createdAt);
}
