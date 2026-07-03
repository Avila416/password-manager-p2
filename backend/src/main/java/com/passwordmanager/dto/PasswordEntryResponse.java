package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Slf4j
public class PasswordEntryResponse {

    private Long id;
    private String username;
    private String password;
    private String strength;
    private Boolean favorite;
    private LocalDateTime createdAt;
}

