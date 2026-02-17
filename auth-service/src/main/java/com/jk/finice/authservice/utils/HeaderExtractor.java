package com.jk.finice.authservice.utils;

import jakarta.servlet.http.HttpServletRequest;

import static com.jk.finice.commonlibrary.constants.AppConstants.IP_ADDRESS_HEADER;
import static com.jk.finice.commonlibrary.constants.AppConstants.USER_AGENT_HEADER;

public class HeaderExtractor {

    public static String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(IP_ADDRESS_HEADER);
        if (forwarded != null && !forwarded.isEmpty()) {
            // The first IP (client's real IP)
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public static String extractUserAgent(HttpServletRequest request) {
        return request.getHeader(USER_AGENT_HEADER);
    }
}
