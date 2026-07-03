package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Slf4j
public class MasterPasswordVerifyDTO {

    @NotBlank(message = "Master password is required")
    private String masterPassword;
}

