package com.skillsphere.auth_service.controller;

import com.skillsphere.auth_service.model.Role;
import com.skillsphere.auth_service.model.User;
import com.skillsphere.auth_service.repository.RoleRepository;
import com.skillsphere.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

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
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }
}