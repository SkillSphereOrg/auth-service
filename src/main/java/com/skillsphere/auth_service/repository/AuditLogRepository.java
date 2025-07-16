package com.skillsphere.auth_service.repository;

import com.skillsphere.auth_service.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}