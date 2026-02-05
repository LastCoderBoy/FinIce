package com.JK.FinIce.authservice.exception;

import java.io.Serial;

public class JwtAuthenticationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public JwtAuthenticationException(String message) {
        super(message);
    }
    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
