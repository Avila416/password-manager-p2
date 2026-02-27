package com.passwordmanager.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class VaultEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String website;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public VaultEntry() {
    }

    public VaultEntry(Long id, String title, String username, String password, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }

    public VaultEntry(Long id, String title, String username, String password, String website, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.website = website;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
