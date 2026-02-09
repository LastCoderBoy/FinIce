package com.JK.FinIce.authservice.service;

import com.JK.FinIce.authservice.entity.EmailToken;
import com.JK.FinIce.authservice.entity.User;
import com.JK.FinIce.authservice.enums.TokenType;

public interface EmailTokenService {

    EmailToken createEmailToken(User user, TokenType tokenType);

    void revokeUserTokens(Long userId, TokenType tokenType);

    User verifyToken(String token, TokenType tokenType);
}
