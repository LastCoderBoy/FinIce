package com.jk.finice.authservice.service;

import com.jk.finice.authservice.entity.EmailToken;
import com.jk.finice.authservice.entity.User;
import com.jk.finice.authservice.enums.TokenType;

public interface EmailTokenService {

    EmailToken createEmailToken(User user, TokenType tokenType);

    void revokeUserTokens(Long userId, TokenType tokenType);

    User verifyToken(String token, TokenType tokenType);
}
