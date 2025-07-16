package com.skillsphere.auth_service.controller;

import com.skillsphere.auth_service.model.User;
import com.skillsphere.auth_service.model.PasswordResetToken;
import com.skillsphere.auth_service.service.PasswordResetService;
import com.skillsphere.auth_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;

    @PostMapping("/request")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        return passwordResetService.findUserByEmail(email)
                .map(user -> {
                    PasswordResetToken token = passwordResetService.createTokenForUser(user);
                    // Compose reset link (in real app, use frontend URL)
                    String resetLink = "https://your-frontend-url/reset-password?token=" + token.getToken();
                    emailService.send(user.getEmail(), "Password Reset Request",
                            "Click the link to reset your password: " + resetLink);
                    return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
                })
                .orElseGet(
                        () -> ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent")));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmReset(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token and new password are required"));
        }
        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
        }
    }
}