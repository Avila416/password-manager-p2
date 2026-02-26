package com.passwordmanager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class GeneratePasswordRequest {

    @Min(value = 8, message = "Length must be at least 8")
    @Max(value = 64, message = "Length must be at most 64")
    private int length; // 8-64
    private boolean uppercase;
    private boolean lowercase;
    private boolean numbers;
    private boolean specialChars;
    private boolean excludeSimilar;
    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 20, message = "Count must be at most 20")
    private int count; // generate multiple
}
