package com.jk.finice.authservice.config.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.finice.authservice.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.jk.finice.commonlibrary.constants.AppConstants.*;

@Service
@Slf4j
public class RedisService {

    private final RedisTemplate<String, String> stringRedisTemplate;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(@Qualifier("blacklistRedisTemplate") RedisTemplate<String, String> stringRedisTemplate,
                        RedisTemplate<String, Object> redisTemplate,
                        ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

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
            stringRedisTemplate.opsForValue().set(key, "revoked", ttlMillis, TimeUnit.MILLISECONDS);
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
            return stringRedisTemplate.hasKey(key); // return true if key exists
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
            stringRedisTemplate.delete(key);
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
            Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("[REDIS-SERVICE] Failed to get TTL: {}", e.getMessage(), e);
            return -1;
        }
    }


    // ========================================
    // USER PROFILE CACHING
    // ========================================

    /**
     * Cache user profile
     * TTL: 30 minutes (configurable)
     *
     * @param userId User ID
     * @param userResponse User profile data
     */
    public void cacheUserProfile(Long userId, UserResponse userResponse) {
        try{
            String key = CACHE_USER_PROFILE_PREFIX + userId;
            redisTemplate.opsForValue().set(key, userResponse, CACHE_USER_PROFILE_TTL, TimeUnit.MINUTES);

            log.info("[REDIS-SERVICE] Cached user profile for user ID: {}", userId);

        } catch (Exception e) {
            log.error("[REDIS-SERVICE] Failed to cache user profile: {}", e.getMessage(), e);
        }
    }

    /**
     * Get cached user profile
     *
     * @param userId User ID
     * @return UserResponse if cached, null if not found or error
     */
    public UserResponse getCachedUserProfile(Long userId) {
        try{
            String key = CACHE_USER_PROFILE_PREFIX + userId;
            Object userObject = redisTemplate.opsForValue().get(key);
            if(userObject != null){
                log.info("[REDIS-SERVICE] Retrieved cached user profile for user ID: {}", userId);
                return objectMapper.convertValue(userObject, UserResponse.class);
            }

            log.debug("[REDIS-SERVICE] Cache MISS for user: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("[REDIS-SERVICE] Failed to get cached user profile: {}", e.getMessage(), e);
            return null; // Fail-safe: fetch from DB
        }
    }

    /**
     * Invalidate user profile cache
     * Call this when user data changes (update, role change, etc.)
     *
     * @param userId User ID
     */
    public void invalidateUserProfile(Long userId) {
        try {
            String key = CACHE_USER_PROFILE_PREFIX + userId;
            redisTemplate.delete(key);
            log.info("[REDIS-SERVICE] Invalidated user profile cache for user: {}", userId);
        } catch (Exception e) {
            log.error("[REDIS-SERVICE] Failed to invalidate user profile: {}", e.getMessage());
        }
    }
}
