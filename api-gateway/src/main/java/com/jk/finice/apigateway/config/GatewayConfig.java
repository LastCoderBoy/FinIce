package com.jk.finice.apigateway.config;

import com.jk.finice.apigateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import static com.jk.finice.commonlibrary.constants.AppConstants.*;

/**
 * API Gateway Route Configuration
 * Defines routing rules for all microservices
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ==========================================
                // SWAGGER/OPENAPI DOCUMENTATION ROUTES
                // (Public - No JWT required)
                // ==========================================

                // Auth Service Swagger
                .route("auth-service-swagger", r -> r
                        .path("/auth-service/v3/api-docs", "/auth-service/swagger-ui/**")
                        .filters(f -> f.rewritePath(
                                "/auth-service/(?<segment>.*)",
                                "/${segment}"))
                        .uri("lb://" + AUTH_SERVICE))

                // Account Service Swagger
                .route("account-service-swagger", r -> r
                        .path("/account-service/v3/api-docs", "/account-service/swagger-ui/**")
                        .filters(f -> f.rewritePath(
                                "/account-service/(?<segment>.*)",
                                "/${segment}"))
                        .uri("lb://" + ACCOUNT_SERVICE))


                // ==========================================
                // PUBLIC PATH (No JWT required)
                // ==========================================
                .route("auth-public", r -> r
                        .path(PUBLIC_PATHS.toArray(new String[0]))
                        .uri("lb://" + AUTH_SERVICE))

                // ==========================================
                // AUTH SERVICE - Authenticated routes
                // ==========================================
                .route("auth-service", r -> r
                        .path(AUTH_PATH + "/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri("lb://" + AUTH_SERVICE))

                // ==========================================
                // ACCOUNT SERVICE - Authenticated routes
                // ==========================================
                .route("account-service", r -> r
                        .path(ACCOUNT_PATH + "/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("accountServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/account"))
                        )
                        .uri("lb://" + ACCOUNT_SERVICE))

                // ==========================================
                // TRANSACTION SERVICE - Authenticated routes
                // ==========================================
                .route("transaction-service", r -> r
                        .path(TRANSACTION_PATH + "/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("transactionServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/transaction"))
                        )
                        .uri("lb://" + TRANSACTION_SERVICE))

                // ==========================================
                // CATCH-ALL / DEFAULT (for unmapped paths)
                // ==========================================
                .route("default-route", r -> r
                        .path("/**")
                        .filters(f -> f.setStatus(HttpStatus.NOT_FOUND))
                        .uri("forward:/fallback/default"))


                .build();
    }
}
