package com.jk.finice.commonlibrary.exception;

import java.io.Serial;

public class UnauthorizedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
