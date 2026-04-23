package com.jk.finice.transactionservice.entity;

import com.jk.finice.commonlibrary.enums.Currency;
import com.jk.finice.transactionservice.enums.TransactionStatus;
import com.jk.finice.transactionservice.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",
        indexes = {
            @Index(name = "idx_transaction_id", columnList = "transaction_id"),
            @Index(name = "idx_source_account_id", columnList = "source_account_id"),
            @Index(name = "idx_destination_account_id", columnList = "destination_account_id"),
            @Index(name = "idx_status", columnList = "status"),
            @Index(name = "idx_transaction_type", columnList = "transaction_type"),
            @Index(name = "idx_created_at", columnList = "created_at"),
            @Index(name = "idx_idempotency_key", columnList = "idempotency_key")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_transaction_id", columnNames = "transaction_id"),
            @UniqueConstraint(name = "uk_reference", columnNames = "reference"),
            @UniqueConstraint(name = "uk_idempotency_key", columnNames = "idempotency_key")
        })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public-facing transaction ID (UUID format)
     * Example: "TXN-550e8400-e29b-41d4-a716-446655440000"
     */
    @Column(name = "transaction_id", unique = true, nullable = false, length = 50)
    private String transactionId;

    /**
     * Idempotency key (prevent duplicate transactions)
     */
    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    /**
     * Bank reference number (for customer support)
     * Example: "REF-20250207-001234"
     */
    @Column(name = "reference", length = 50, unique = true)
    private String reference;


    @Enumerated(EnumType.STRING)
    @Column( name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    // ============ Account Details ============

    @Column(name = "source_account_id", nullable = false)
    private Long sourceAccountId;

    @Column(name = "destination_account_id")
    private Long destinationAccountId;

    // IBAN snapshot
    @Column(name = "sender_iban", nullable = false, length = 34)
    private String senderIban;

    @Column(name = "receiver_iban", length = 34)
    private String receiverIban;  // nullable for DEPOSIT (no sender outside system)


    // ============ Money Details ============
    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    // ============ Status Transaction ============

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // ============ Metadata ============
    @Column(name = "description", length = 500)
    private String description;

    // ============ Timestamps ============
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ============ Audit ============
    @Column(name = "created_by", nullable = false)
    private Long createdBy;             // User ID who initiated the transaction

    // ============================================
    // Helper Methods
    // ============================================

    public void markCompleted() {
        this.status = TransactionStatus.COMPLETE;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
    }

    public void markCancelled() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Can only cancel PENDING transactions");
        }
        this.status = TransactionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isCancellable() {
        return this.status == TransactionStatus.PENDING;
    }

    /**
     * Check if transaction is final (completed/failed/cancelled)
     */
    public boolean isFinal() {
        return this.status == TransactionStatus.COMPLETE ||
                this.status == TransactionStatus.FAILED ||
                this.status == TransactionStatus.CANCELLED;
    }
}
