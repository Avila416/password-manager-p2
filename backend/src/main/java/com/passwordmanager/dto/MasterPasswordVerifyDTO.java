package com.passwordmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MasterPasswordVerifyDTO {

    @NotBlank(message = "Master password is required")
    private String masterPassword;
}
