package com.JK.FinIce.commonlibrary.constants;

import java.util.List;

public final class AppConstants {

    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("AppConstants is a utility class and cannot be instantiated");
    }

    // ========== HTTP Headers ==========
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String SERVICE_NAME_HEADER = "X-Service-Name";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String IP_ADDRESS_HEADER = "X-Forwarded-For";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String USERNAME_HEADER = "X-Username";

    // ========== Service Names ==========
    public static final String EUREKA_SERVER = "eureka-server";
    public static final String API_GATEWAY = "api-gateway";
    public static final String AUTH_SERVICE = "auth-service";
    public static final String ACCOUNT_SERVICE = "account-service";
    public static final String TRANSACTION_SERVICE = "transaction-service";
    public static final String CARD_SERVICE = "card-service";
    public static final String LOAN_SERVICE = "loan-service";
    public static final String NOTIFICATION_SERVICE = "notification-service";
    public static final String REPORTING_SERVICE = "reporting-service";


    // ========== API Endpoints ==========
    public static final String API_VERSION = "v1";
    public static final String BASE_PATH = "/api/" + API_VERSION;
    public static final String AUTH_PATH = BASE_PATH + "/auth";
    public static final String ACCOUNT_PATH = BASE_PATH + "/accounts";
    public static final String TRANSACTION_PATH = BASE_PATH + "/transactions";
    public static final String CARD_PATH = BASE_PATH + "/cards";
    public static final String LOAN_PATH = BASE_PATH + "/loans";
    public static final String NOTIFICATION_PATH = BASE_PATH + "/notifications";
    public static final String REPORT_PATH = BASE_PATH + "/reports";

    public static final List<String> PUBLIC_PATHS = List.of(
            AUTH_PATH + "/register",
            AUTH_PATH + "/login",
            AUTH_PATH + "/refresh-token",
            AUTH_PATH + "/verify-email/**",
            AUTH_PATH + "/forgot-password",
            AUTH_PATH + "/reset-password",

            // Actuator endpoints
            "/actuator/health",
            "/actuator/info",

            // Swagger/API docs
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**"
    );


    // ========== JWT ==========
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
    public static final long ACCESS_TOKEN_DURATION_MS = 15 * 60 * 1000; // 15 minutes
    public static final long REFRESH_TOKEN_DURATION_MS = 7 * 24 * 60 * 60 * 1000; // 7 days
    public static final String JWT_CLAIM_USER_ID = "userId";
    public static final String JWT_CLAIM_ROLES = "roles";
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_TOKEN_TYPE = "tokenType";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";


    // ========== Cache Keys ==========
    public static final String CACHE_USER_PREFIX = "user:";
    public static final String CACHE_TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    public static final String CACHE_REFRESH_TOKEN_PREFIX = "refresh:token:";
    public static final String CACHE_OTP_PREFIX = "otp:";
    public static final String CACHE_SESSION_PREFIX = "session:";

    // ========== Cache TTL (in seconds) ==========
    public static final long CACHE_USER_TTL = 3600; // 1 hour
    public static final long CACHE_TOKEN_BLACKLIST_TTL = 86400; // 24 hours
    public static final long CACHE_OTP_TTL = 300; // 5 minutes
    public static final long CACHE_SESSION_TTL = 1800; // 30 minutes

    // ========== Pagination ==========
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";


}
