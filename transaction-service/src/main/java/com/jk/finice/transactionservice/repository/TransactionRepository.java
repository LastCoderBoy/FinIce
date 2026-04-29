package com.jk.finice.transactionservice.repository;

import com.jk.finice.transactionservice.entity.Transaction;
import com.jk.finice.transactionservice.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByIdempotencyKey(String resolvedKey);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.senderIban = :iban " +
            "AND t.sourceAccountId = :accountId " +
            "AND t.transactionType = 'TRANSFER' " +
            "AND t.status in ('COMPLETE', 'PENDING') " +
            "AND t.createdAt >= :startOfDay")
    BigDecimal dailyTransferredAmount(String iban, Long accountId,
                                      LocalDateTime startOfDay);

    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.transactionId = :transactionId AND t.createdBy = :userId")
    Optional<Transaction> findByTransactionIdAndCreatedBy(String transactionId, Long userId);

}
