package com.passwordmanager.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertResponse {

    private Long id;
    private String message;
    private String severity;
    private String type;
    private LocalDateTime createdAt;
}
