package com.jk.finice.authservice.service;

import com.jk.finice.authservice.entity.RefreshToken;
import com.jk.finice.authservice.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String clientIP, String userAgent);

    RefreshToken findByToken(String token);

    RefreshToken verifyRefreshToken(String token);

    RefreshToken rotateRefreshToken(RefreshToken oldToken, String clientIP, String userAgent);

    void revokeRefreshToken(String token);

    void revokeAllRefreshTokensAsync(Long userId);
}
