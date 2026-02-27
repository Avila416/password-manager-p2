package com.passwordmanager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.passwordmanager.entity.BackupFile;
import com.passwordmanager.entity.PasswordEntry;
import com.passwordmanager.exception.BackupException;
import com.passwordmanager.repository.BackupFileRepository;
import com.passwordmanager.repository.PasswordEntryRepository;
import com.passwordmanager.service.AuditService;
import com.passwordmanager.service.BackupService;
import com.passwordmanager.util.AuditActions;
import com.passwordmanager.security.EncryptionUtil;
import com.passwordmanager.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class BackupServiceImpl implements BackupService {

    private final BackupFileRepository backupFileRepository;
    private final PasswordEntryRepository passwordEntryRepository;
    private final EncryptionUtil encryptionUtil;
    private final FileUtil fileUtil;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final String backupStoragePath;

    public BackupServiceImpl(BackupFileRepository backupFileRepository,
                             PasswordEntryRepository passwordEntryRepository,
                             EncryptionUtil encryptionUtil,
                             FileUtil fileUtil,
                             AuditService auditService,
                             ObjectMapper objectMapper,
                             JdbcTemplate jdbcTemplate,
                             @Value("${backup.storage.path:backup}") String backupStoragePath) {
        this.backupFileRepository = backupFileRepository;
        this.passwordEntryRepository = passwordEntryRepository;
        this.encryptionUtil = encryptionUtil;
        this.fileUtil = fileUtil;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.backupStoragePath = backupStoragePath;
    }

    @Override
    public String exportBackup() {
        String vaultSnapshot = serializeVaultSnapshot();
        String encrypted = encryptionUtil.encrypt(vaultSnapshot);
        String checksum = sha256(encrypted);
        String payload = checksum + "." + encrypted;

        String fileName = "vault-backup-" + System.currentTimeMillis() + ".bkp";
        BackupFile backupFile = new BackupFile();
        backupFile.setFileName(fileName);
        backupFile.setEncryptedContent(encrypted);
        backupFile.setChecksum(checksum);
        backupFile.setCreatedAt(LocalDateTime.now());
        backupFileRepository.save(backupFile);
        writeBackupFile(fileName, payload);

        auditService.log(AuditActions.BACKUP_EXPORT, "127.0.0.1", "SUCCESS");
        return payload;
    }

    @Override
    @Transactional
    public Map<String, Object> restoreBackup(String fileContent) {
        if (!isValidPayload(fileContent)) {
            auditService.log(AuditActions.BACKUP_RESTORE, "127.0.0.1", "FAILED");
            throw new BackupException("Invalid backup content");
        }
        String encrypted = getEncryptedSegment(fileContent);
        String decrypted;
        try {
            decrypted = encryptionUtil.decrypt(encrypted);
        } catch (Exception ex) {
            auditService.log(AuditActions.BACKUP_RESTORE, "127.0.0.1", "FAILED");
            throw new BackupException("Unable to decrypt backup content");
        }

        int restoredEntries = restoreVaultSnapshot(decrypted);
        auditService.log(AuditActions.BACKUP_RESTORE, "127.0.0.1", "SUCCESS");
        return Map.of(
                "message", "Backup restored successfully",
                "restoredEntries", restoredEntries,
                "restoredAt", LocalDateTime.now().toString()
        );
    }

    private String serializeVaultSnapshot() {
        try {
            List<PasswordEntry> entries = passwordEntryRepository.findAll();
            return objectMapper.writeValueAsString(entries);
        } catch (Exception ex) {
            throw new BackupException("Unable to generate backup payload");
        }
    }

    private int restoreVaultSnapshot(String decryptedPayload) {
        try {
            List<PasswordEntry> entries = objectMapper.readValue(
                    decryptedPayload,
                    new TypeReference<List<PasswordEntry>>() {
                    }
            );
            jdbcTemplate.execute("TRUNCATE TABLE password_entry");
            entries.forEach(entry -> {
                entry.setId(null);
                if (entry.getCreatedAt() == null) {
                    entry.setCreatedAt(LocalDateTime.now());
                }
                if (entry.getFavorite() == null) {
                    entry.setFavorite(false);
                }
                if (entry.getTitle() == null) {
                    entry.setTitle("");
                }
                if (entry.getWebsite() == null) {
                    entry.setWebsite("");
                }
                if (entry.getCategory() == null || entry.getCategory().isBlank()) {
                    entry.setCategory("OTHER");
                }
            });
            passwordEntryRepository.saveAll(entries);
            return entries.size();
        } catch (Exception ex) {
            throw new BackupException("Unable to restore vault entries from backup content");
        }
    }

    @Override
    public Map<String, Object> updateBackup(String fileContent) {
        if (!isValidPayload(fileContent)) {
            auditService.log(AuditActions.BACKUP_UPDATE, "127.0.0.1", "FAILED");
            throw new BackupException("Invalid backup content");
        }
        List<BackupFile> backups = backupFileRepository.findAll();
        if (backups.isEmpty()) {
            auditService.log(AuditActions.BACKUP_UPDATE, "127.0.0.1", "FAILED");
            throw new BackupException("No backup found to update");
        }
        BackupFile latest = backups.stream().max(Comparator.comparing(BackupFile::getCreatedAt)).orElseThrow();
        if (!fileUtil.validate(latest.getFileName())) {
            latest.setFileName("vault-backup-" + System.currentTimeMillis() + ".bkp");
        }
        latest.setEncryptedContent(getEncryptedSegment(fileContent));
        latest.setChecksum(getChecksumSegment(fileContent));
        latest.setCreatedAt(LocalDateTime.now());
        backupFileRepository.save(latest);
        writeBackupFile(latest.getFileName(), fileContent);
        auditService.log(AuditActions.BACKUP_UPDATE, "127.0.0.1", "SUCCESS");
        Path path = resolveBackupPath(latest.getFileName());
        return Map.of(
                "message", "Backup updated successfully",
                "fileName", latest.getFileName(),
                "filePath", path.toString(),
                "checksum", latest.getChecksum(),
                "payloadLength", fileContent.length(),
                "updatedAt", latest.getCreatedAt().toString()
        );
    }

    @Override
    public Map<String, Object> validateBackup(String fileContent) {
        boolean valid = isValidPayload(fileContent);
        auditService.log(AuditActions.BACKUP_VALIDATE, "127.0.0.1", valid ? "SUCCESS" : "FAILED");
        if (!valid) {
            throw new BackupException("Backup integrity validation failed");
        }
        String checksum = getChecksumSegment(fileContent);
        String encrypted = getEncryptedSegment(fileContent);
        return Map.of(
                "message", "Backup file validated successfully",
                "checksum", checksum,
                "encryptedLength", encrypted.length(),
                "payloadLength", fileContent.length(),
                "validatedAt", LocalDateTime.now().toString()
        );
    }

    @Override
    public String deleteBackup() {
        List<BackupFile> backups = backupFileRepository.findAll();
        if (backups.isEmpty()) {
            auditService.log(AuditActions.BACKUP_DELETE, "127.0.0.1", "FAILED");
            throw new BackupException("No backup found to delete");
        }
        BackupFile latest = backups.stream().max(Comparator.comparing(BackupFile::getCreatedAt)).orElseThrow();
        deleteBackupFile(latest.getFileName());
        backupFileRepository.deleteById(latest.getId());
        auditService.log(AuditActions.BACKUP_DELETE, "127.0.0.1", "SUCCESS");
        return "Backup deleted successfully";
    }

    @Override
    public Map<String, Object> latestBackupInfo() {
        List<BackupFile> backups = backupFileRepository.findAll();
        if (backups.isEmpty()) {
            return Map.of(
                    "exists", false,
                    "fileName", "",
                    "filePath", "",
                    "createdAt", ""
            );
        }
        BackupFile latest = backups.stream().max(Comparator.comparing(BackupFile::getCreatedAt)).orElseThrow();
        String fileName = latest.getFileName();
        if (!fileUtil.validate(fileName)) {
            return Map.of(
                    "exists", false,
                    "fileName", "",
                    "filePath", "",
                    "createdAt", latest.getCreatedAt() == null ? "" : latest.getCreatedAt().toString()
            );
        }
        Path path = resolveBackupPath(fileName);
        return Map.of(
                "exists", Files.exists(path),
                "fileName", fileName,
                "filePath", path.toString(),
                "createdAt", latest.getCreatedAt() == null ? "" : latest.getCreatedAt().toString()
        );
    }

    private boolean isValidPayload(String fileContent) {
        if (!fileUtil.validate(fileContent) || !fileContent.contains(".")) {
            return false;
        }
        String checksum = getChecksumSegment(fileContent);
        String encrypted = getEncryptedSegment(fileContent);
        if (!fileUtil.validate(checksum) || !fileUtil.validate(encrypted)) {
            return false;
        }
        if (!checksum.equals(sha256(encrypted))) {
            return false;
        }

        // Supports both legacy base64 payloads and current versioned payloads (e.g. v1:...).
        try {
            encryptionUtil.decrypt(encrypted);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String getChecksumSegment(String fileContent) {
        return fileContent.substring(0, fileContent.indexOf('.'));
    }

    private String getEncryptedSegment(String fileContent) {
        return fileContent.substring(fileContent.indexOf('.') + 1);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new BackupException("Unable to compute backup checksum");
        }
    }

    private void writeBackupFile(String fileName, String payload) {
        if (!fileUtil.validate(fileName)) {
            throw new BackupException("Invalid backup file name");
        }
        Path directory = Paths.get(backupStoragePath).toAbsolutePath().normalize();
        Path filePath = resolveBackupPath(fileName);
        if (!filePath.startsWith(directory)) {
            throw new BackupException("Invalid backup storage path");
        }
        try {
            Files.createDirectories(directory);
            Files.writeString(filePath, payload, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            throw new BackupException("Unable to write backup file");
        }
    }

    private void deleteBackupFile(String fileName) {
        if (!fileUtil.validate(fileName)) {
            return;
        }
        Path directory = Paths.get(backupStoragePath).toAbsolutePath().normalize();
        Path filePath = resolveBackupPath(fileName);
        if (!filePath.startsWith(directory)) {
            return;
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Keep API delete behavior focused on DB state even if file cleanup fails.
        }
    }

    private Path resolveBackupPath(String fileName) {
        Path directory = Paths.get(backupStoragePath).toAbsolutePath().normalize();
        return directory.resolve(fileName).normalize();
    }
}
