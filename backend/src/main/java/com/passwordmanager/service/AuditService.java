package com.passwordmanager.service;

import com.passwordmanager.dto.AuditLogResponse;

import java.util.List;

public interface AuditService {

    void log(String action, String ip, String status);

    List<AuditLogResponse> getLogs();
}