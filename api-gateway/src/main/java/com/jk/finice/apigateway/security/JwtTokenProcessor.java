package com.jk.finice.apigateway.security;

import com.jk.finice.commonlibrary.dto.JwtClaimsPayload;
import com.jk.finice.commonlibrary.exception.InternalServerException;
import com.jk.finice.commonlibrary.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.jk.finice.commonlibrary.constants.AppConstants.*;

@Component
@Slf4j
public class JwtTokenProcessor {

    private SecretKey secretKey;

    @Value( "${jwt.secret}")
    private String key;

    @PostConstruct
    public void init() {
        try{
            byte[] decodedBytes = Decoders.BASE64.decode(key);
            this.secretKey = Keys.hmacShaKeyFor(decodedBytes); // throws WeakKeyException if the key is too weak

            log.info("[JWT-PROCESSOR] Initialized successfully");
            log.info("[JWT-PROCESSOR] Token validity: {} ms ({} minutes)",
                    ACCESS_TOKEN_DURATION_MS, ACCESS_TOKEN_DURATION_MS / 60000);

        } catch (IllegalArgumentException ie) {
            log.error("[JWT-PROCESSOR] Invalid JWT secret key: {}", ie.getMessage());
            throw new InvalidTokenException("Invalid JWT secret key");
        } catch (Exception e) {
            log.error("[JWT-PROCESSOR] Unexpected error occurred while initializing JWT provider: {}", e.getMessage());
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    public Optional<JwtClaimsPayload> validateAndExtractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            Long userId = extractUserId(claims);
            List<String> roles = getRolesFromToken(claims);

            return Optional.of(new JwtClaimsPayload(userId, email, roles));
        } catch (ExpiredJwtException e) {
            log.warn("[JWT-PROCESSOR] Token expired: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException e) {
            log.warn("[JWT-PROCESSOR] Invalid token: {}", e.getMessage());
            return Optional.empty();
        }
    }


    private Long extractUserId(Claims claims) {
        Object userIdKey = claims.get(JWT_CLAIM_USER_ID);

        if (userIdKey instanceof Integer) {
            return ((Integer) userIdKey).longValue();
        } else if (userIdKey instanceof Long) {
            return (Long) userIdKey;
        } else if (userIdKey != null) {
            return Long.parseLong(userIdKey.toString());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(Claims claims) {
        try {
            Object roles = claims.get(JWT_CLAIM_ROLES);

            if (roles instanceof List) {
                return (List<String>) roles;
            }

            return List.of();
        } catch (Exception e) {
            log.error("[JWT-PROCESSOR] Failed to extract roles from token: {}", e.getMessage());
            return List.of(); // Return empty list if roles not found
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();

        } catch (Exception e) {
            log.error("[JWT-PROCESSOR] Failed to extract expiration from token: {}", e.getMessage());
            throw new InvalidTokenException("Failed to extract expiration from token");
        }
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }
}
