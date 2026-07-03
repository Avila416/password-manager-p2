package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Slf4j
public class AuditResponse {

    private int total;
    private int weak;
    private int reused;
    private int old;
    private Long reportId;
    private int alertCount;
    private LocalDateTime generatedAt;
}

