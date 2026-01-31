package com.JK.FinIce.authservice.service.impl;

import com.JK.FinIce.authservice.entity.RefreshToken;
import com.JK.FinIce.authservice.entity.User;
import com.JK.FinIce.authservice.repository.RefreshTokenRepository;
import com.JK.FinIce.authservice.service.RefreshTokenService;
import com.JK.FinIce.commonlibrary.exception.ResourceNotFoundException;
import com.JK.FinIce.commonlibrary.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.REFRESH_TOKEN_DURATION_MS;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String clientIP, String userAgent) {
        // Generate a secure random token
        String tokenString = TokenUtils.generateSecureToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .user(user)
                .expiresAt(Instant.now().plusMillis(REFRESH_TOKEN_DURATION_MS))
                .revoked(false)
                .ipAddress(clientIP)
                .userAgent(userAgent)
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("[REFRESH-TOKEN-SERVICE] Created refresh token for user: {} (ID: {})",
                user.getUsername(), user.getId());

        return refreshToken;
    }

    @Override
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("[REFRESH-TOKEN-SERVICE] Refresh token not found");
                    return new ResourceNotFoundException("Invalid refresh token");
                });
    }


    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, String clientIP, String userAgent) {
        oldToken.revoke();
        refreshTokenRepository.save(oldToken);

        log.info("[REFRESH-TOKEN-SERVICE] Rotated refresh token for user: {}",
                oldToken.getUser().getUsername());

        return createRefreshToken(oldToken.getUser(), clientIP, userAgent);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
        log.info("[REFRESH-TOKEN-SERVICE] Revoked refresh token for user: {}", refreshToken.getUser().getUsername());
    }

    /**
     * Revoke all refresh tokens for a user (used on password change, logout from all devices)
     */
    @Override
    @Transactional
    public void revokeAllRefreshTokens(Long userId) {
        int revokedCount = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("[REFRESH-TOKEN-SERVICE] Revoked {} refresh tokens for user: {}", revokedCount, userId);
    }
}
