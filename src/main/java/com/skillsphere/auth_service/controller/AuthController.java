package com.skillsphere.auth_service.controller;

import com.skillsphere.auth_service.model.Role;
import com.skillsphere.auth_service.model.User;
import com.skillsphere.auth_service.repository.RoleRepository;
import com.skillsphere.auth_service.repository.UserRepository;
import com.skillsphere.auth_service.security.JwtUtil;
import com.skillsphere.auth_service.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuditService auditService;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.get("username"), request.get("password")));
            User user = userRepository.findByUsername(request.get("username")).orElseThrow();
            String token = jwtUtil.generateToken(
                    request.get("username"),
                    user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
            auditService.log("LOGIN", request.get("username"), "User logged in successfully");
            return Collections.singletonMap("token", token);
        } catch (AuthenticationException e) {
            auditService.log("LOGIN_FAIL", request.get("username"), "Invalid username or password");
            throw new RuntimeException("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> request) {
        if (userRepository.findByUsername(request.get("username")).isPresent()) {
            auditService.log("REGISTER_FAIL", request.get("username"), "Username already exists");
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.get("email")).isPresent()) {
            auditService.log("REGISTER_FAIL", request.get("username"), "Email already exists");
            throw new RuntimeException("Email already exists");
        }
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));
        User user = new User();
        user.setUsername(request.get("username"));
        user.setEmail(request.get("email"));
        user.setPassword(passwordEncoder.encode(request.get("password")));
        user.setEnabled(true);
        user.setRoles(Collections.singleton(userRole));
        userRepository.save(user);
        auditService.log("REGISTER", user.getUsername(), "User registered successfully");
        return Collections.singletonMap("message", "User registered successfully");
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "roles", user.getRoles().stream().map(r -> r.getName()).toArray());
    }
}