package com.jk.finice.apigateway.controller;


import com.jk.finice.commonlibrary.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Fallback Controller for Circuit Breaker
 * Provides user-friendly error messages when services are down
 *
 * @author LastCoderBoy
 * @since 2025-01-24
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    /**
     * Fallback for Auth Service
     */
    @RequestMapping(value = "/auth", method = {GET, POST, PUT, DELETE, PATCH})
    public ResponseEntity<ApiResponse<Void>> authServiceFallback() {
        log.error("[FALLBACK] Auth service is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Authentication service is temporarily unavailable. Please try again later."
                ));
    }

    /**
     * Fallback for Transaction Service
     */
    @RequestMapping(value = "/transaction", method = {GET, POST, PUT, DELETE, PATCH})
    public ResponseEntity<ApiResponse<Void>> transactionServiceFallback() {
        log.error("[FALLBACK] Transaction service is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Service is temporarily unavailable. Please try again later."
                ));
    }

    /**
     * Fallback for Account Service
     */
    @RequestMapping(value = "/account", method = {GET, POST, PUT, DELETE, PATCH})
    public ResponseEntity<ApiResponse<Void>> accountServiceFallback() {
        log.error("[FALLBACK] Account service is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Service is temporarily unavailable. Please try again later."
                ));
    }

    /**
     * Generic fallback
     */
    @RequestMapping(value = "/default", method = {GET, POST, PUT, DELETE, PATCH})
    public ResponseEntity<ApiResponse<Void>> defaultFallback() {
        log.error("[FALLBACK] Service is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Service is temporarily unavailable. Our team has been notified. Please try again later."
                ));
    }
}
