package com.passwordmanager.vault.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.passwordmanager.vault.entity.Category;

import jakarta.validation.constraints.Size;

public class UpdatePasswordEntryDTO {

    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 120, message = "Username cannot exceed 120 characters")
    private String username;

    @Size(min = 4, max = 256, message = "Password length must be between 4 and 256")
    private String password;

    @Size(max = 200, message = "Website cannot exceed 200 characters")
    private String website;

    private Category category;
    private Boolean favorite;

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

    @JsonAlias({"password", "encryptedPassword"})
    public void setPassword(String password) {
        this.password = password;
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

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }
}
