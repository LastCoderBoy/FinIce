package com.JK.FinIce.apigateway.exception;

import com.JK.FinIce.commonlibrary.dto.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        // Default to 500 for unexpected errors
        log.error("[API-GATEWAY] Internal Server Error: {}", ex.getMessage(), ex);
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String getUserFriendlyMessage(Throwable ex, HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) {
            return "The requested resource was not found";
        }

        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "Service is temporarily unavailable. Please try again later";
        }

        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return "Request timeout. Please try again";
        }

        if (status.is5xxServerError()) {
            return "An error occurred while processing your request. Please try again later";
        }

        // For client errors, return the actual message
        return ex.getMessage() != null ? ex.getMessage() : "An error occurred";
    }
}
