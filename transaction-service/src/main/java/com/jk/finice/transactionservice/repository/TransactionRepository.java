package com.jk.finice.transactionservice.repository;

import com.jk.finice.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String resolvedKey);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.senderIban = :iban " +
            "AND t.sourceAccountId = :accountId " +
            "AND t.transactionType = 'TRANSFER' " +
            "AND t.status in ('COMPLETE', 'PENDING') " +
            "AND t.createdAt >= :startOfDay")
    BigDecimal dailyTransferredAmount(String iban, Long accountId,
                                      LocalDateTime startOfDay);

}
