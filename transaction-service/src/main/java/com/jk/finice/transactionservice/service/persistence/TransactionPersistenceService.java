package com.jk.finice.transactionservice.service.persistence;

import com.jk.finice.commonlibrary.exception.ValidationException;
import com.jk.finice.transactionservice.dto.response.PersistResult;
import com.jk.finice.transactionservice.entity.Transaction;
import com.jk.finice.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionPersistenceService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Optional<Transaction> checkIdempotency(String resolvedKey) {
        return transactionRepository.findByIdempotencyKey(resolvedKey);
    }

    @Transactional(readOnly = true)
    public BigDecimal getDailyTransferredAmount(String iban, Long accountId) {
        return transactionRepository.dailyTransferredAmount(
                iban, accountId, LocalDate.now().atStartOfDay()
        );
    }

    @Transactional
    public PersistResult persistPending(Transaction transaction, String resolvedKey) {
        try {
            Transaction saved = transactionRepository.save(transaction);
            return new PersistResult(saved, true);   // <- winner
        } catch (DataIntegrityViolationException e) {
            log.info("[PERSISTENCE] Duplicate idempotency key: {}", resolvedKey);
            Transaction existing = transactionRepository.findByIdempotencyKey(resolvedKey)
                    .orElseThrow(() -> new ValidationException("Duplicate idempotency key"));
            return new PersistResult(existing, false); // <- loser
        }
    }

    @Transactional
    public void markComplete(Transaction transaction) {
        transaction.markCompleted();
        transactionRepository.save(transaction);
    }

    @Transactional
    public void markFailed(Transaction transaction, String reason) {
        transaction.markFailed(reason);
        transactionRepository.save(transaction);
    }
}

