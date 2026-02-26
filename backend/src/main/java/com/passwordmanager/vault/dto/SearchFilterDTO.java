package com.passwordmanager.vault.dto;

import com.passwordmanager.vault.entity.Category;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SearchFilterDTO {

    @Size(max = 100, message = "Keyword cannot exceed 100 characters")
    private String keyword;

    private Category category;

    @Pattern(regexp = "^(title|createdAt|date)?$", message = "sortBy must be title or createdAt")
    private String sortBy;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}
