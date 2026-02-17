package com.jk.finice.authservice.service.impl;

import com.jk.finice.authservice.entity.EmailToken;
import com.jk.finice.authservice.entity.User;
import com.jk.finice.authservice.enums.TokenType;
import com.jk.finice.authservice.repository.EmailTokenRepository;
import com.jk.finice.authservice.service.EmailTokenService;
import com.jk.finice.commonlibrary.exception.InvalidTokenException;
import com.jk.finice.commonlibrary.exception.ResourceNotFoundException;
import com.jk.finice.commonlibrary.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTokenServiceImpl implements EmailTokenService {

    private final JavaMailSender javaMailSender;
    private final EmailTokenRepository emailTokenRepository;

    @Value("${spring.mail.username}")
    private String emailFrom;


    private static final int EMAIL_VERIFICATION_EXPIRY_MINUTES = 15;
    private static final int PASSWORD_RESET_EXPIRY_MINUTES = 10;

    @Override
    @Transactional
    public EmailToken createEmailToken(User user, TokenType tokenType) {
        // Revoke any existing unused tokens
        revokeUserTokens(user.getId(), tokenType);

        // Generate new token
        String tokenString = TokenUtils.generateSecureToken();

        int expiryMinutes = switch (tokenType) {
            case EMAIL_VERIFICATION, EMAIL_CHANGE_CONFIRMATION -> EMAIL_VERIFICATION_EXPIRY_MINUTES;
            case PASSWORD_RESET -> PASSWORD_RESET_EXPIRY_MINUTES;
        };

        EmailToken emailToken = EmailToken.builder()
                .token(tokenString)
                .tokenType(tokenType)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();

        emailToken = emailTokenRepository.save(emailToken);

        log.info("[EMAIL-TOKEN-SERVICE] Created {} token for user: {} (expires in {} min)",
                tokenType, user.getId(), expiryMinutes);

        return emailToken;
    }

    @Override
    @Transactional
    public void revokeUserTokens(Long userId, TokenType tokenType) {
        int revokedCount = emailTokenRepository.revokeUnusedTokensByUserAndType(userId, tokenType);
        if (revokedCount > 0) {
            log.info("[EMAIL-TOKEN-SERVICE] Revoked {} {} tokens for user: {}",
                    revokedCount, tokenType, userId);
        }
    }

    @Override
    @Transactional
    public User verifyToken(String token, TokenType tokenType) {
        // Find token
        EmailToken emailToken = emailTokenRepository.findByTokenAndTokenType(token, tokenType)
                .orElseThrow(() -> {
                    log.warn("[EMAIL-TOKEN-SERVICE] Token not found: {}", token);
                    return new ResourceNotFoundException("Invalid or expired token");
                });

        // Check if already used
        if (emailToken.isUsed()) {
            log.warn("[EMAIL-TOKEN-SERVICE] Token already used: {}", token);
            throw new InvalidTokenException("Token has already been used");
        }

        // Check if expired
        if (emailToken.isExpired()) {
            log.warn("[EMAIL-TOKEN-SERVICE] Token expired: {}", token);
            throw new InvalidTokenException("Token has expired. Please request a new one.");
        }

        // Mark as used
        emailToken.markAsUsed();
        emailTokenRepository.save(emailToken);

        log.info("[EMAIL-TOKEN-SERVICE] Token verified successfully for user: {}",
                emailToken.getUser().getId());

        return emailToken.getUser();
    }

}
