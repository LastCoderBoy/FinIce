package com.JK.FinIce.apigateway.security;

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

    // TODO: Add the auth-service dependency
    private final PathMatcher pathMatcher;
    private final JwtProvider jwtProvider;
    @Autowired
    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        super(Config.class);
        this.jwtProvider = jwtProvider;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            log.info("[JWT-AUTH-FILTER] Processing request: {} {}", request.getMethod(), path);

            if(isPublicPath(path, config)){
                log.info("[JWT-AUTH-FILTER] Request is public, skipping authentication");
                return chain.filter(exchange);
            }

            // Extract the JWT token from the Authorization header and validate it
            if(!request.getHeaders().containsKey(AUTHORIZATION_HEADER)){
                log.warn("[JWT-AUTH-FILTER] No Authorization header found in request");
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            // TODO: Consider validating the roles
            String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
            try{
                String token = TokenUtils.validateAndExtractToken(authHeader); // throws InvalidTokenException if token is invalid
                if(!jwtProvider.validateToken(token)){
                    log.warn("[JWT-AUTH-FILTER] Invalid JWT token");
                    return onError(exchange, config.getUnauthorizedMessage(), HttpStatus.UNAUTHORIZED);
                }

                // TODO: Deep validation by calling the AUTHORIZATION-SERVICE for blacklisted tokens
                // will implement later

                // Extract username and role from JWT token and set it in the request context
                String username = jwtProvider.getUsernameFromJWT(token);
                List<String> roles = jwtProvider.getRolesFromToken(token);
                log.info("[JWT-AUTH-FILTER] User {} authenticated successfully", username);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(USER_ID_HEADER, username)
                        .header(USER_ROLES_HEADER, String.join(",", roles))
                        .build();

                // Return the request to the responsible backend service
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (InvalidTokenException e){
                throw e;
            } catch (Exception e){
                log.error("[JWT-AUTH-FILTER] Unexpected error occurred while validating JWT token: {}", e.getMessage());
                return onError(exchange, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
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
