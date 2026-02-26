package com.passwordmanager.repository;

import com.passwordmanager.entity.AuditReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditReportRepository extends JpaRepository<AuditReport, Long> {
}
