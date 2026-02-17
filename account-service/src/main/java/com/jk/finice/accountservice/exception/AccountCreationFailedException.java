package com.jk.finice.accountservice.exception;

import java.io.Serial;

public class AccountCreationFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AccountCreationFailedException(String message) {
        super(message);
    }

    public AccountCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
