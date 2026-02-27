package com.jk.finice.accountservice.enums;

/**
 * Lifecycle status of a bank account
 */
public enum AccountStatus {

    /**
     * Account is active and operational
     * - All operations allowed
     */
    ACTIVE,

    /**
     * Account permanently closed
     * - No operations allowed
     * - Balance must be zero
     * - Cannot be reopened
     */
    CLOSED
}
