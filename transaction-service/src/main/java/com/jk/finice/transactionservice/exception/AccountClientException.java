package com.jk.finice.transactionservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class AccountClientException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public AccountClientException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    public AccountClientException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getHttpStatus() {
        return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
