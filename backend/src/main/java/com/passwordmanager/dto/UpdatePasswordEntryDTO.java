package com.passwordmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordEntryDTO {

    @NotBlank(message = "Username is required")
    @Size(max = 120, message = "Username cannot exceed 120 characters")
    private String username;

    @Size(min = 4, max = 256, message = "Password length must be between 4 and 256")
    private String password;

    private String title;
    private String website;
    private String category;
}
