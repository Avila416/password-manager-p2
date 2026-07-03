package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Slf4j
public class RestoreRequest {

    @NotBlank(message = "Backup content is required")
    @Size(max = 2000000, message = "Backup content is too large")
    private String fileContent;

    public RestoreRequest() {
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}

