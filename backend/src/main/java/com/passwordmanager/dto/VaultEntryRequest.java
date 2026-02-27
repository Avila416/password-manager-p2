package com.passwordmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VaultEntryRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @NotBlank(message = "Username is required")
    @Size(max = 120, message = "Username must be at most 120 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 300, message = "Password must be between 6 and 300 characters")
    private String password;

    @Size(max = 200, message = "Website must be at most 200 characters")
    private String website;

    public VaultEntryRequest() {
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
}
