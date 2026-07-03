package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Slf4j
public class PasswordResponse {

    private String password;
    private String strength; // WEAK/MEDIUM/STRONG/VERY_STRONG
}

