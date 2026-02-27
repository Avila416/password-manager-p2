package com.passwordmanager.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PasswordEntryResponseDTO {
    private Long id;
    private String title;
    private String username;
    private String website;
    private String category;
    private boolean favorite;
    private String password;
    private String strength;
    private LocalDateTime createdAt;
}
