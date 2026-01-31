package com.JK.FinIce.authservice.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.CACHE_TOKEN_BLACKLIST_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Add token to blacklist
     *
     * @param token JWT token to blacklist
     * @param ttlMillis Time to live in milliseconds (should match token expiration)
     */
    public void blackListToken(String token, long ttlMillis) {
        try{
            if(ttlMillis <= 0){
                log.warn("[REDIS-SERVICE] TTL is already expired, skipping blacklisting");
                return;
            }
            String key = CACHE_TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "revoked", ttlMillis, TimeUnit.MILLISECONDS);
            log.info("[REDIS-SERVICE] Blacklisted token: {}", token);

        } catch (Exception e) {
            log.error("[REDIS-SERVICE] Failed to blacklist token: {}", e.getMessage());
        }
    }

    /**
     * Check if token is blacklisted
     *
     * @param token JWT token to check
     * @return true if blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        try{
            String key = CACHE_TOKEN_BLACKLIST_PREFIX + token;
            Boolean existInCache = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(existInCache)) {
                log.debug("[REDIS-SERVICE] Token found in blacklist");
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("[REDIS-SERVICE] Error checking blacklist: {}", e.getMessage(), e);
            // Fail-safe: If Redis is down, allow the request
            // (JWT expiration will still be enforced)

            return false;
        }
    }

    /**
     * Remove token from blacklist (for testing/admin purposes)
     *
     * @param token JWT token to remove
     */
    public void removeTokenFromBlacklist(String token) {
        try {
            String key = CACHE_TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.delete(key);
            log.info("[REDIS-SERVICE] Token removed from blacklist");
        } catch (Exception e) {
            log.error("[REDIS-SERVICE] Failed to remove token from blacklist: {}", e.getMessage(), e);
        }
    }

    /**
     * Get remaining TTL for blacklisted token
     *
     * @param token JWT token
     * @return TTL in seconds, or -1 if not found
     */
    public long getTokenBlacklistTTL(String token) {
        try {
            String key = CACHE_TOKEN_BLACKLIST_PREFIX + token;
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("[REDIS-SERVICE] Failed to get TTL: {}", e.getMessage(), e);
            return -1;
        }
    }
}
