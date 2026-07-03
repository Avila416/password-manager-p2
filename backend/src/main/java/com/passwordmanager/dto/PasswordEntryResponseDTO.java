package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Slf4j
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

