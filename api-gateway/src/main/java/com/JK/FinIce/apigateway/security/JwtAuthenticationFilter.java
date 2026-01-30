package com.JK.FinIce.apigateway.security;

import com.JK.FinIce.apigateway.redis.RedisService;
import com.JK.FinIce.commonlibrary.exception.InvalidTokenException;
import com.JK.FinIce.commonlibrary.utils.TokenUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.*;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final PathMatcher pathMatcher;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    @Autowired
    public JwtAuthenticationFilter(JwtProvider jwtProvider, RedisService redisService) {
        super(Config.class);
        this.jwtProvider = jwtProvider;
        this.redisService = redisService;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            log.info("[JWT-AUTH-FILTER] Processing request: {} {}", request.getMethod(), path);

            if (isPublicPath(path, config)) {
                log.info("[JWT-AUTH-FILTER] Public path detected, skipping authentication");
                return chain.filter(exchange);
            }

            if (!config.isEnabled()) {
                log.debug("[JWT-AUTH-FILTER] Filter disabled for this route");
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(AUTHORIZATION_HEADER)) {
                log.warn("[JWT-AUTH-FILTER] Missing Authorization header for: {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

            try {
                String token = TokenUtils.validateAndExtractToken(authHeader);

                if (!jwtProvider.validateToken(token)) {
                    log.warn("[JWT-AUTH-FILTER] Invalid JWT token for path: {}", path);
                    return onError(exchange, config.getUnauthorizedMessage(), HttpStatus.UNAUTHORIZED);
                }

                // Check if token is blacklisted
                return redisService.isTokenBlacklisted(token)
                        .flatMap(isBlacklisted -> {
                            if (isBlacklisted) {
                                log.warn("[JWT-AUTH-FILTER] Blacklisted token attempted for: {}", path);
                                return onError(exchange, "Token has been revoked", HttpStatus.UNAUTHORIZED);
                            }

                            // Extract user details
                            String userId = String.valueOf(jwtProvider.getUserIdFromToken(token));
                            String username = jwtProvider.getUsernameFromJWT(token);
                            List<String> userRoles = jwtProvider.getRolesFromToken(token);

                            // Check required roles
                            if (!config.getRequiredRoles().isEmpty()) {
                                boolean hasRequiredRole = userRoles.stream()
                                        .anyMatch(role -> config.getRequiredRoles().contains(role));

                                if (!hasRequiredRole) {
                                    log.warn("[JWT-AUTH-FILTER] User {} lacks required roles for {}",
                                            username, path);
                                    return onError(exchange, config.getForbiddenMessage(), HttpStatus.FORBIDDEN);
                                }
                            }

                            log.info("[JWT-AUTH-FILTER] User {} (ID: {}) authenticated successfully",
                                    username, userId);

                            // Add user context headers
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header(USER_ID_HEADER, userId != null ? userId : "")
                                    .header(USERNAME_HEADER, username)
                                    .header(USER_ROLES_HEADER, String.join(",", userRoles))
                                    .build();

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        });

            } catch (InvalidTokenException e) {
                log.warn("[JWT-AUTH-FILTER] Invalid token: {}", e.getMessage());
                return onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.error("[JWT-AUTH-FILTER] Unexpected error: {}", e.getMessage(), e);
                return onError(exchange, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String errorJson = String.format(
                "{\"success\": false, \"message\": \"%s\", \"data\": null}",
                message
        );

        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorJson.getBytes())));

    }

    private boolean isPublicPath(String path, Config config) {
        boolean isGlobalPath = PUBLIC_PATHS.stream().anyMatch(
                pattern -> pathMatcher.match(pattern, path));

        boolean isRoutePublic = config.getPublicPaths() != null &&
                !config.getPublicPaths().isEmpty() &&
                config.getPublicPaths().stream().anyMatch(pattern ->
                        pathMatcher.match(pattern, path));

        return isGlobalPath || isRoutePublic;
    }


    /**
     * Configuration class for per-route filter customization
     */
    @Getter
    @Setter
    public static class Config {

        /**
         * Enable/disable JWT authentication for this route
         */
        private boolean enabled = true;

        /**
         * Additional public paths for this specific route (supports wildcards)
         */
        private List<String> publicPaths = new ArrayList<>();

        /**
         * Required roles for accessing this route
         * If empty, any authenticated user can access
         */
        private List<String> requiredRoles = new ArrayList<>();

        /**
         * Custom headers to add to downstream requests
         */
        private Map<String, String> customHeaders = new HashMap<>();

        /**
         * Custom unauthorized message
         */
        private String unauthorizedMessage = "Unauthorized access";

        /**
         * Custom forbidden message
         */
        private String forbiddenMessage = "Insufficient permissions";
    }
}
