package com.passwordmanager.repository;

import com.passwordmanager.entity.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {

    List<PasswordEntry> findAllByOrderByCreatedAtDesc();

    List<PasswordEntry> findAllByFavoriteTrueOrderByCreatedAtDesc();

    long deleteByUsername(String username);
}
