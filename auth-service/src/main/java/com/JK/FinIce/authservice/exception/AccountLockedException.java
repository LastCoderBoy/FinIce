package com.JK.FinIce.authservice.exception;

import java.io.Serial;

public class AccountLockedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AccountLockedException(String message) {
        super(message);
    }
    public AccountLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
