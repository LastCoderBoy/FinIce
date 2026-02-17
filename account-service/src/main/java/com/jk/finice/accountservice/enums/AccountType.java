package com.jk.finice.accountservice.enums;

public enum AccountType {
    /**
     * Savings Account
     * - Earns interest (2-4% APY)
     * - Limited transactions (5-10 per month)
     * - Lower daily limits
     * - Minimum balance required
     */
    SAVINGS,

    /**
     * Current/Checking Account
     * - No interest
     * - Unlimited transactions
     * - Higher daily limits
     * - May have overdraft facility
     */
    CURRENT
}
