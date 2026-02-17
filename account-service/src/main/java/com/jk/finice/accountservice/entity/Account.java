package com.jk.finice.accountservice.entity;

import com.jk.finice.accountservice.enums.AccountType;
import com.jk.finice.accountservice.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "accounts",
        indexes = {
                @Index(name = "idx_account_number", columnList = "account_number"),
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_account_type", columnList = "account_type")
        },
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_number", columnNames = "account_number")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 16)
    private String accountNumber;

    // ========== Ownership ==========

    @Column(name = "user_id", nullable = false)
    private Long userId;  // References User in auth-service

    // ========== Account Details ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "account_nick_name", length = 100)
    private String accountNickName; // Probably will be mostly used on Savings accounts

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    @Builder.Default
    private Currency currency = Currency.USD;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO; // the money which you see in the account

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO; // the money which you can use NOW

    @Column(name = "hold_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal holdAmount = BigDecimal.ZERO; // the money which is in pending transactions

    // ========== Limits & Rules ==========

    @Column(name = "daily_withdrawal_limit", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal dailyWithdrawalLimit = BigDecimal.valueOf(10000);

    @Column(name = "daily_transfer_limit", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal dailyTransferLimit = BigDecimal.valueOf(10000);


    // ========== Interest (for Savings/Fixed Deposit) ==========

    @Column(name = "interest_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal interestRate = BigDecimal.ZERO;  // Annual interest rate (e.g., 3.5%)

    @Column(name = "last_interest_calculated_at")
    private LocalDateTime lastInterestCalculatedAt;

    // ========== Metadata ==========

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;


    // ========== Helper Methods ==========

    /**
     * Credit amount to account
     */
    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }

    /**
     * Debit amount from account
     */
    public void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }

    /**
     * Place hold on funds (for pending transactions)
     */
    public void placeHold(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hold amount must be positive");
        }
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient available balance");
        }
        this.holdAmount = this.holdAmount.add(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    /**
     * Release hold on funds
     */
    public void releaseHold(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hold amount must be positive");
        }
        if (this.holdAmount.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Hold amount cannot be negative");
        }
        this.holdAmount = this.holdAmount.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }
}
