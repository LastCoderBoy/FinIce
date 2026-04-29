package com.jk.finice.transactionservice.exception.handler;

import com.jk.finice.commonlibrary.dto.ApiResponse;
import com.jk.finice.commonlibrary.exception.*;
import com.jk.finice.transactionservice.exception.AccountClientException;
import com.jk.finice.transactionservice.exception.TransactionFailedException;
import jakarta.ws.rs.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class TransactionExceptionHandler {

    /**
     * Handle validation errors (from @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("[TRANSACTION-EXCEPTION-HANDLER] Validation error: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(AccountClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountClientException(AccountClientException ex) {
        log.error("[TRANSACTION-EXCEPTION-HANDLER] Account client exception: {}", ex.getMessage());

        HttpStatus status = ex.getHttpStatus();

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage()));
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("[TRANSACTION-EXCEPTION-HANDLER] Resource not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        log.error("[TRANSACTION-EXCEPTION-HANDLER] Validation exception: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AccountClosedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountClosedException(AccountClosedException ex){
        log.error("[TRANSACTION-EXCEPTION-HANDLER] Account closed: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("[TRANSACTION-EXCEPTION-HANDLER] Unauthorized exception: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(TransactionFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionFailedException(TransactionFailedException ex) {
        log.error("[TRANSACTION-EXCEPTION-HANDLER] Transaction failed exception: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage()));
    }


    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ApiResponse<Void>> handleService(InternalServerException ex) {
        log.error("[TRANSACTION-EXCEPTION-HANDLER] Service exception: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An error occurred while processing your request"));
    }


    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("[TRANSACTION-EXCEPTION-HANDLER] Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}
