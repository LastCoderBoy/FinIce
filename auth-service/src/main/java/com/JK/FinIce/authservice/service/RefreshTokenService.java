package com.JK.FinIce.authservice.service;

import com.JK.FinIce.authservice.entity.RefreshToken;
import com.JK.FinIce.authservice.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String clientIP, String userAgent);

    RefreshToken findByToken(String token);

    RefreshToken rotateRefreshToken(RefreshToken oldToken, String clientIP, String userAgent);

    void revokeRefreshToken(String token);

    void revokeAllRefreshTokens(Long userId);
}
