package com.passwordmanager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordResponse {

    private String password;
    private String strength; // WEAK/MEDIUM/STRONG/VERY_STRONG
}
