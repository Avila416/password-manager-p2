package com.passwordmanager.repository;

import com.passwordmanager.entity.BackupFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupFileRepository extends JpaRepository<BackupFile, Long> {
}
