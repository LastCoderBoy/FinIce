package com.JK.FinIce.authservice.config.security;


import com.JK.FinIce.commonlibrary.exception.InternalServerException;
import com.JK.FinIce.commonlibrary.exception.InvalidTokenException;
import com.JK.FinIce.commonlibrary.utils.TokenUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.*;

@Component
@Slf4j
public class JwtProvider {

    private SecretKey secretKey;

    @Value( "${jwt.secret}")
    private String key;

    @PostConstruct
    public void init() {
        try{
            byte[] decodedBytes = Decoders.BASE64.decode(key);
            this.secretKey = Keys.hmacShaKeyFor(decodedBytes); // throws WeakKeyException if the key is too weak

            log.info("[AUTH-JWT-PROVIDER] Initialized successfully");
            log.info("[AUTH-JWT-PROVIDER] Token validity: {} ms ({} minutes)",
                    ACCESS_TOKEN_DURATION_MS, ACCESS_TOKEN_DURATION_MS / 60000);

        } catch (IllegalArgumentException ie) {
            log.error("[AUTH-JWT-PROVIDER] Invalid JWT secret key: {}", ie.getMessage());
            throw new InvalidTokenException("Invalid JWT secret key");
        } catch (Exception e) {
            log.error("[AUTH-JWT-PROVIDER] Unexpected error occurred while initializing JWT provider: {}", e.getMessage());
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    public String generateAccessToken(String username, Long userId, String email, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JWT_CLAIM_USER_ID, userId);
        claims.put(JWT_CLAIM_ROLES, roles);
        claims.put(JWT_CLAIM_EMAIL, email);
        claims.put(JWT_CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_DURATION_MS);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Validate JWT token
     * Checks signature, expiration, and format
     *
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build().parseSignedClaims(token);

            log.info("[AUTH-JWT-PROVIDER] JWT token is valid for user: {}", claims.getPayload().getSubject());
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[AUTH-JWT-PROVIDER] Token expired for user: {}",
                    e.getClaims() != null ? e.getClaims().getSubject() : "unknown");
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("[AUTH-JWT-PROVIDER] JWT token compact of handler are invalid. {}", e.getMessage());
            return false;
        } catch (MalformedJwtException me) {
            log.warn("[AUTH-JWT-PROVIDER] Malformed JWT token: {}", me.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("[AUTH-JWT-PROVIDER] Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[AUTH-JWT-PROVIDER] Unexpected error occurred while verifying JWT token: {}", e.getMessage());
            return false;
        }
    }

    // ==================== TOKEN EXTRACTION ====================

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
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

    public String getEmailFromToken(String token) {
        return extractAllClaims(token).get(JWT_CLAIM_EMAIL, String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object roles = claims.get(JWT_CLAIM_ROLES);

            if (roles instanceof List) {
                return (List<String>) roles;
            }

            return List.of();
        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Failed to extract roles: {}", e.getMessage());
            return List.of();
        }
    }

    public String getTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get(JWT_CLAIM_TOKEN_TYPE, String.class);
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
            log.error("[AUTH-JWT-PROVIDER] Failed to extract expiration from token: {}", e.getMessage());
            throw new InvalidTokenException("Failed to extract expiration from token");
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }

    // ==================== HELPER METHODS ====================

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("[AUTH-JWT-PROVIDER] Failed to extract claims: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Extract specific claim from token
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
}

