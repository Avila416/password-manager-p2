package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Slf4j
public class AlertResponse {

    private Long id;
    private String message;
    private String severity;
    private String type;
    private LocalDateTime createdAt;
}

