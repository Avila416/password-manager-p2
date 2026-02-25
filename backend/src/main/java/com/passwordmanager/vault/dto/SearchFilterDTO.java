package com.passwordmanager.vault.dto;

import com.passwordmanager.vault.entity.Category;

import lombok.Data;

@Data
public class SearchFilterDTO {

    private String keyword;
    private Category category;
    private String sortBy; // title or createdAt

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