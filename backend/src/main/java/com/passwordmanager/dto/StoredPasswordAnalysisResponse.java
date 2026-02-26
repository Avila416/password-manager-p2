package com.passwordmanager.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StoredPasswordAnalysisResponse {

    private Long entryId;
    private String username;
    private String website;
    private String strength;
    private boolean weak;
    private boolean reused;
    private boolean old;
    private LocalDateTime createdAt;
}
