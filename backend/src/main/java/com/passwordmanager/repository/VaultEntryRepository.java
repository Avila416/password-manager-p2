package com.passwordmanager.repository;

import com.passwordmanager.entity.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {

    long countByCreatedAtAfter(LocalDateTime time);
}
