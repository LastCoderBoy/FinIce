package com.JK.FinIce.apigateway.security;

import com.JK.FinIce.commonlibrary.exception.InternalServerException;
import com.JK.FinIce.commonlibrary.exception.InvalidTokenException;
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

import static com.JK.FinIce.commonlibrary.constants.AppConstants.ACCESS_TOKEN_DURATION_MS;

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

            log.info("[JWT-PROVIDER] Initialized successfully");
            log.info("[JWT-PROVIDER] Token validity: {} ms ({} minutes)",
                    ACCESS_TOKEN_DURATION_MS, ACCESS_TOKEN_DURATION_MS / 60000);

        } catch (IllegalArgumentException ie) {
            log.error("[JWT-PROVIDER] Invalid JWT secret key: {}", ie.getMessage());
            throw new InvalidTokenException("Invalid JWT secret key");
        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Unexpected error occurred while initializing JWT provider: {}", e.getMessage());
            throw new InternalServerException("Unexpected error occurred!");
        }
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

            log.info("[JWT-PROVIDER] JWT token is valid for user: {}", claims.getPayload().getSubject());
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT-PROVIDER] Token expired: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("[JWT-PROVIDER] JWT token compact of handler are invalid. {}", e.getMessage());
            return false;
        } catch (MalformedJwtException me) {
            log.warn("[JWT-PROVIDER] Malformed JWT token: {}", me.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("[JWT-PROVIDER] Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Unexpected error occurred while verifying JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username from JWT token
     *
     * @param token JWT token
     * @return username (subject)
     */
    public String getUsernameFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build().parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException e) {
            log.warn("[JWT-PROVIDER] Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT token");
        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Unexpected error occurred while extracting username from JWT token: {}", e.getMessage());
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("roles", List.class);
        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Failed to extract roles from token: {}", e.getMessage());
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
            log.error("[JWT-PROVIDER] Failed to extract expiration from token: {}", e.getMessage());
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
