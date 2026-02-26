package com.passwordmanager.vault.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Category {

    SOCIAL,
    BANKING,
    WORK,
    SHOPPING,
    OTHER
    ;

    @JsonCreator
    public static Category from(String value) {
        if (value == null) {
            return null;
        }
        return Category.valueOf(value.trim().toUpperCase());
    }
}
