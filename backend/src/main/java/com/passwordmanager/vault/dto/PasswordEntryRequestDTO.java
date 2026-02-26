package com.passwordmanager.vault.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.passwordmanager.vault.entity.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PasswordEntryRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Username is required")
    @Size(max = 120, message = "Username cannot exceed 120 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 256, message = "Password length must be between 4 and 256")
    private String encryptedPassword;

    @NotBlank(message = "Website is required")
    @Size(max = 200, message = "Website cannot exceed 200 characters")
    private String website;

    @NotNull(message = "Category is required")
    private Category category;

    private LocalDateTime createdAt;

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
        return encryptedPassword;
    }

    @JsonAlias({"password", "encryptedPassword"})
    public void setPassword(String password) {
        this.encryptedPassword = password;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
