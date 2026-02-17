package com.jk.finice.authservice.exception;

import java.io.Serial;

public class DuplicateResourceFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicateResourceFoundException(String message) {
        super(message);
    }
    public DuplicateResourceFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
