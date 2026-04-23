package com.jk.finice.transactionservice.exception.handler;

import com.jk.finice.commonlibrary.dto.ApiResponse;
import com.jk.finice.commonlibrary.exception.InternalServerException;
import com.jk.finice.commonlibrary.exception.ResourceNotFoundException;
import com.jk.finice.commonlibrary.exception.UnauthorizedException;
import com.jk.finice.commonlibrary.exception.ValidationException;
import com.jk.finice.transactionservice.exception.AccountClientException;
import com.jk.finice.transactionservice.exception.TransactionFailedException;
import jakarta.ws.rs.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class TransactionExceptionHandler {

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
}
