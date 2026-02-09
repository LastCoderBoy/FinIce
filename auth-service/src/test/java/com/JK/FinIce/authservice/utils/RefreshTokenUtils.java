package com.JK.FinIce.authservice.utils;

import com.JK.FinIce.authservice.entity.RefreshToken;
import com.JK.FinIce.authservice.entity.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RefreshTokenUtils {

    public static RefreshToken buildRefreshToken(User user, String ipAddress, String userAgent) {
        return RefreshToken.builder()
                .token("test-refresh-token-" + System.currentTimeMillis())
                .user(user)
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS)) // Expires in 30 days
                .revoked(false)
                .revokedAt(null)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                // createdAt is handled by @CreationTimestamp
                .build();
    }

    public static RefreshToken buildRevokedRefreshToken(User user, String ipAddress, String userAgent) {
        return RefreshToken.builder()
                .token("revoked-refresh-token-" + System.currentTimeMillis())
                .user(user)
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS)) // Already expired
                .revoked(true)
                .revokedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                // createdAt is handled by @CreationTimestamp
                .build();
    }

    public static RefreshToken buildExpiredRefreshToken(User user, String ipAddress, String userAgent) {
        return RefreshToken.builder()
                .token("expired-refresh-token-" + System.currentTimeMillis())
                .user(user)
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS)) // Expired yesterday
                .revoked(false)
                .revokedAt(null)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                // createdAt is handled by @CreationTimestamp
                .build();
    }
}

