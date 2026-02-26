package com.passwordmanager.vault.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.passwordmanager.vault.entity.PasswordEntry;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
    List<PasswordEntry> findByFavoriteTrue();
    List<PasswordEntry> findByWebsiteContainingIgnoreCase(String website);
}
