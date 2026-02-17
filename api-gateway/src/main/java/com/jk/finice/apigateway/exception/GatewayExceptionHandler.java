package com.jk.finice.apigateway.exception;

import com.jk.finice.commonlibrary.dto.ApiResponse;
import com.jk.finice.commonlibrary.exception.InvalidTokenException;
import com.jk.finice.commonlibrary.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Exception handler for API Gateway
 * Handles gateway-level errors (routing failures, circuit breaker, etc.)
 *
 * @author LastCoderBoy
 * @since 2026-01-22
 */
@Component
@Order(-1)
@Slf4j
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        HttpStatus status = determineStatus(ex);

        // Set the status code and content type
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<String> response = ApiResponse.error(getUserFriendlyMessage(ex, status));

        try{
            byte[] responseBytes = objectMapper.writeValueAsBytes(response);
            DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(responseBytes);

            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        } catch (JsonProcessingException e) {
            log.error("[API-GATEWAY] Error while serializing error response: {}", e.getMessage(), e);
            return exchange.getResponse().setComplete();
        }
    }

    private HttpStatus determineStatus(Throwable ex) {
        if(ex instanceof ResponseStatusException){
            log.error("[API-GATEWAY] Client Error: {}", ex.getMessage(), ex);
            return (HttpStatus) ((ResponseStatusException) ex).getStatusCode();
        }
        if (ex instanceof InvalidTokenException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof io.netty.handler.timeout.TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        if (ex instanceof java.net.ConnectException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        // Default to 500 for unexpected errors
        log.error("[API-GATEWAY] Internal Server Error: {}", ex.getMessage(), ex);
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String getUserFriendlyMessage(Throwable ex, HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "The requested resource was not found";
            case UNAUTHORIZED -> "Authentication required. Please log in";
            case FORBIDDEN -> "You don't have permission to access this resource";
            case SERVICE_UNAVAILABLE -> "Service is temporarily unavailable. Please try again later";
            case GATEWAY_TIMEOUT -> "Request timeout. Please try again";
            case TOO_MANY_REQUESTS -> "Too many requests. Please slow down";
            default -> {
                if (status.is5xxServerError()) {
                    yield "An internal error occurred. Please try again later";
                }
                // For 4xx errors, return the actual message
                yield ex.getMessage() != null ? ex.getMessage() : "An error occurred";
            }
        };
    }
}
