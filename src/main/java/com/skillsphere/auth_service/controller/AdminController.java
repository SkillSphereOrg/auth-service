package com.skillsphere.auth_service.controller;

import com.skillsphere.auth_service.model.Role;
import com.skillsphere.auth_service.model.User;
import com.skillsphere.auth_service.repository.RoleRepository;
import com.skillsphere.auth_service.repository.UserRepository;
import com.skillsphere.auth_service.repository.AuditLogRepository;
import com.skillsphere.auth_service.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private AuditService auditService;

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        return userRepository.findAll().stream().map(user -> Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())))
                .collect(Collectors.toList());
    }

    @PutMapping("/user/{id}/roles")
    public Map<String, Object> assignRoles(@PathVariable Long id, @RequestBody Set<String> roles) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        Set<Role> roleEntities = roles.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(new Role(null, roleName))))
                .collect(Collectors.toSet());
        user.setRoles(roleEntities);
        userRepository.save(user);
        auditService.log("ASSIGN_ROLES", user.getUsername(), "Assigned roles: " + roles);
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }

    @GetMapping("/analytics")
    public Map<String, Object> getAnalytics() {
        long userCount = userRepository.count();
        long adminCount = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))).count();
        long auditLogCount = auditLogRepository.count();
        return Map.of(
                "totalUsers", userCount,
                "adminUsers", adminCount,
                "auditLogEntries", auditLogCount);
    }

    @GetMapping("/audit-logs/export")
    public ResponseEntity<byte[]> exportAuditLogsAsCsv() {
        var logs = auditLogRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("id,action,username,details,timestamp\n");
        for (var log : logs) {
            csv.append(log.getId()).append(",")
                    .append(escapeCsv(log.getAction())).append(",")
                    .append(escapeCsv(log.getUsername())).append(",")
                    .append(escapeCsv(log.getDetails())).append(",")
                    .append(log.getTimestamp()).append("\n");
        }
        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audits.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }
}