package com.JK.FinIce.commonlibrary.constants;

public class AppConstants {

    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("AppConstants is a utility class and cannot be instantiated");
    }

    public static final String BEARER = "Bearer ";

    // ========== HTTP Headers ==========
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String SERVICE_NAME_HEADER = "X-Service-Name";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String IP_ADDRESS_HEADER = "X-Forwarded-For";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

}
