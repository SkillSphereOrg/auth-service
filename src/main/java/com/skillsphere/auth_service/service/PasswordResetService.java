package com.skillsphere.auth_service.service;

import com.skillsphere.auth_service.model.PasswordResetToken;
import com.skillsphere.auth_service.model.User;
import com.skillsphere.auth_service.repository.PasswordResetTokenRepository;
import com.skillsphere.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.token-expiry-minutes:30}")
    private long tokenExpiryMinutes;

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public PasswordResetToken createTokenForUser(User user) {
        // Remove any existing tokens for this user
        tokenRepository.deleteByUserId(user.getId());
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(tokenExpiryMinutes, ChronoUnit.MINUTES);
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiry(expiry)
                .build();
        return tokenRepository.save(resetToken);
    }

    public Optional<PasswordResetToken> validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> t.getExpiry().isAfter(Instant.now()));
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = validateToken(token);
        if (tokenOpt.isEmpty())
            return false;
        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteByUserId(user.getId());
        return true;
    }
}