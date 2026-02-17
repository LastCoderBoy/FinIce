package com.jk.finice.apigateway.redis;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.jk.finice.commonlibrary.constants.AppConstants.CACHE_TOKEN_BLACKLIST_PREFIX;

/**
 * Redis Service for API Gateway (Reactive operations)
 * Handles token blacklist checks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    /**
     * Check if token is blacklisted (Reactive)
     *
     * @param token JWT token to check
     * @return Mono<Boolean> true if blacklisted, false otherwise
     */
    public Mono<Boolean> isTokenBlacklisted(String token) {
        try {
            String key = CACHE_TOKEN_BLACKLIST_PREFIX + token;

            return reactiveRedisTemplate.hasKey(key)
                    .doOnSuccess(exists -> {
                        if (Boolean.TRUE.equals(exists)) {
                            log.debug("[REDIS-SERVICE] Token found in blacklist");
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("[REDIS-SERVICE] Error checking blacklist: {}", e.getMessage());
                        // Fail-safe: Allow request if Redis is down
                        return Mono.just(false);
                    })
                    .defaultIfEmpty(false);

        } catch (Exception e) {
            log.error("[REDIS-SERVICE] Unexpected error: {}", e.getMessage(), e);
            return Mono.just(false);
        }
    }
}
