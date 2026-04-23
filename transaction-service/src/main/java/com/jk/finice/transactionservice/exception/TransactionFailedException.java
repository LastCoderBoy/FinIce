package com.jk.finice.transactionservice.exception;

import java.io.Serial;

public class TransactionFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TransactionFailedException(String message) {
        super(message);
    }

    public TransactionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
