package com.jk.finice.commonlibrary.exception;

import java.io.Serial;

public class AccountClosedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AccountClosedException(String message) {
        super(message);
    }

    public AccountClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
