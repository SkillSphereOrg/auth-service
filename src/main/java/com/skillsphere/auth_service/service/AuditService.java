package com.skillsphere.auth_service.service;

import com.skillsphere.auth_service.model.AuditLog;
import com.skillsphere.auth_service.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {
    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String action, String username, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .username(username)
                .details(details)
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(log);
    }
}