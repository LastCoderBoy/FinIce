package com.jk.finice.authservice.exception;

import java.io.Serial;

public class AccountNotVerifiedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AccountNotVerifiedException(String message) {
        super(message);
    }
    public AccountNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
