package com.passwordmanager.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class BackupFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String encryptedContent;

    @Column(nullable = false)
    private String checksum;

    private LocalDateTime createdAt;

    public BackupFile() {
    }

    public BackupFile(Long id, String fileName, String encryptedContent, String checksum, LocalDateTime createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.encryptedContent = encryptedContent;
        this.checksum = checksum;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
