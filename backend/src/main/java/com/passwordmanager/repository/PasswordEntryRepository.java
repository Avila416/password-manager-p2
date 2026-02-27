package com.passwordmanager.repository;

import com.passwordmanager.entity.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {

    List<PasswordEntry> findAllByOrderByCreatedAtDesc();

    List<PasswordEntry> findAllByFavoriteTrueOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime time);

    long deleteByUsername(String username);
}
