package com.jk.finice.transactionservice.service.impl;

import com.jk.finice.commonlibrary.enums.Currency;
import com.jk.finice.commonlibrary.exception.ResourceNotFoundException;
import com.jk.finice.commonlibrary.exception.UnauthorizedException;
import com.jk.finice.commonlibrary.exception.ValidationException;
import com.jk.finice.transactionservice.dto.client.AccountClientResponse;
import com.jk.finice.transactionservice.dto.client.CreditRequest;
import com.jk.finice.transactionservice.dto.client.DebitRequest;
import com.jk.finice.transactionservice.dto.client.HoldRequest;
import com.jk.finice.transactionservice.dto.request.ExternalTransferRequest;
import com.jk.finice.transactionservice.dto.request.InternalTransferRequest;
import com.jk.finice.transactionservice.dto.response.PersistResult;
import com.jk.finice.transactionservice.dto.response.TransferResponse;
import com.jk.finice.transactionservice.entity.Transaction;
import com.jk.finice.transactionservice.enums.TransactionStatus;
import com.jk.finice.transactionservice.enums.TransactionType;
import com.jk.finice.transactionservice.exception.TransactionFailedException;
import com.jk.finice.transactionservice.mapper.TransactionMapper;
import com.jk.finice.transactionservice.repository.TransactionRepository;
import com.jk.finice.transactionservice.service.TransactionService;
import com.jk.finice.transactionservice.service.client.AccountServiceClient;
import com.jk.finice.transactionservice.service.persistence.TransactionPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import static com.jk.finice.commonlibrary.utils.TokenUtils.generateSecureToken;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final ExecutorService virtualThreadExecutor;
    private final AccountServiceClient accountServiceClient;
    private final TransactionPersistenceService persistenceService;
    private final TransactionRepository transactionRepository;


    @Override
    public TransferResponse internalTransfer(InternalTransferRequest transferRequest, Long userId, String idempotencyKey) {

        // self-transfer guard first, no point fetching accounts
        if(transferRequest.getSourceAccountId()
                .equals(transferRequest.getDestinationAccountId())) {
            throw new ValidationException("Cannot transfer to the same account");
        }

        // Generate or use client provided idempotency key
        String resolvedKey = resolveKey(idempotencyKey);

        // Check DB for existing transaction with this key
        Optional<Transaction> existing = persistenceService.checkIdempotency(resolvedKey);

        if (existing.isPresent()) {
            Transaction existingTxn = existing.get();
            log.info("[TRANSACTION-SERVICE] Duplicate request detected, returning existing transaction: {}",
                    existingTxn.getTransactionId());
            return TransactionMapper.toTransferResponse(existingTxn); // return original result, no processing
        }

        // Parallel fetch on virtual threads
        CompletableFuture<AccountClientResponse> senderFuture =
                CompletableFuture.supplyAsync(
                        () -> accountServiceClient.getAccountInternal(transferRequest.getSourceAccountId()),
                        virtualThreadExecutor
                );

        CompletableFuture<AccountClientResponse> receiverFuture =
                CompletableFuture.supplyAsync(
                        () -> accountServiceClient.getAccountInternal(
                                transferRequest.getDestinationAccountId()),
                        virtualThreadExecutor
                );

        // Wait for both
        AccountClientResponse senderClient;
        AccountClientResponse receiverClient;

        try {
            CompletableFuture.allOf(senderFuture, receiverFuture).join();
            senderClient = senderFuture.join();
            receiverClient = receiverFuture.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            log.error("[TRANSACTION-SERVICE] Failed to fetch account details: {}",
                    cause.getMessage());
            // unwrap and rethrow known exceptions
            if (cause instanceof ResourceNotFoundException ex) throw ex;
            if (cause instanceof UnauthorizedException ex) throw ex;
            if (cause instanceof ValidationException ex) throw ex;
            throw new TransactionFailedException(
                    "Failed to fetch account details: " + cause.getMessage(), cause);
        }

        // Validate sender and receiver accounts
        validateSenderAccount(
                senderClient, transferRequest, userId,
                persistenceService.getDailyTransferredAmount(
                        senderClient.getIban(), senderClient.getAccountId())
        );
        validateReceiverAccount(receiverClient, senderClient.getCurrency());


        String transactionId = generateTransactionId();
        String reference = generateReference();

        // Persist PENDING transaction
        Transaction transaction = buildPendingTransaction(
                transferRequest, resolvedKey, transactionId,
                reference, senderClient, receiverClient, userId
        );

        PersistResult persistResult = persistenceService.persistPending(transaction, resolvedKey);

        // Loser returns immediately no side effects
        if (!persistResult.isOwner()) {
            log.info("[TRANSACTION-SERVICE] Concurrent duplicate detected, returning existing: {}",
                    persistResult.getTransaction().getTransactionId());
            return TransactionMapper.toTransferResponse(persistResult.getTransaction());
        }

        // Only winner Thread continues from here
        Transaction savedTransaction = persistResult.getTransaction();



        boolean holdPlaced = false;
        boolean debitExecuted = false;
        boolean creditExecuted = false;

        // Execute debit -> credit
        try {
            accountServiceClient.placeHold(
                    senderClient.getAccountId(),
                    new HoldRequest(transferRequest.getAmount(), transactionId)
            );
            holdPlaced = true;

            accountServiceClient.debitAccount(
                    senderClient.getAccountId(),
                    new DebitRequest(transferRequest.getAmount(), transactionId)
            );
            debitExecuted = true;

            accountServiceClient.creditAccount(
                    receiverClient.getAccountId(),
                    new CreditRequest(transferRequest.getAmount(), transactionId)
            );
            creditExecuted = true;

            persistenceService.markComplete(savedTransaction);

        } catch (Exception e) {
            log.error("[TRANSACTION-SERVICE] Transfer failed for transaction ID: {}", transactionId, e);
            handleCompensation(
                    holdPlaced, debitExecuted, creditExecuted, senderClient,
                    receiverClient, transferRequest, transactionId
            );

            persistenceService.markFailed(savedTransaction, e.getMessage());

            if (e instanceof ValidationException ||
                    e instanceof UnauthorizedException ||
                    e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new TransactionFailedException("Transfer failed: " + e.getMessage(), e);
        }

        return TransactionMapper.toTransferResponse(savedTransaction);
    }

    @Override
    public TransferResponse externalTransfer(ExternalTransferRequest transferRequest, Long userId) {
        return null;
    }


    // =====================================================
    //                     HELPER METHODS
    // =====================================================

    private Transaction buildPendingTransaction(InternalTransferRequest request,
                                                String resolvedKey,
                                                String transactionId,
                                                String reference,
                                                AccountClientResponse sender,
                                                AccountClientResponse receiver,
                                                Long userId) {
        return Transaction.builder()
                .transactionId(transactionId)
                .reference(reference)
                .idempotencyKey(resolvedKey)
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .sourceAccountId(request.getSourceAccountId())
                .destinationAccountId(request.getDestinationAccountId())
                .senderIban(sender.getIban())
                .receiverIban(receiver.getIban())
                .amount(request.getAmount())
                .currency(sender.getCurrency())
                .description(request.getDescription())
                .createdBy(userId)
                .build();
    }

    private String generateTransactionId() {
        // System identifier: longer, more entropy, not customer facing
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "TXN-" + date + "-" + random;
        // Output: TXN-20250422-A3F8C2E1D4B7
    }

    private String generateReference() {
        // Customer facing: shorter, easier to read/quote to support
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = generateSecureToken().substring(0, 6).toUpperCase();
        return "REF-" + date + "-" + suffix;
        // Output: REF-20250422-X7K2P9
    }

    private String resolveKey(String idempotencyKey) {
        return (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();
    }

    private void handleCompensation(boolean holdPlaced, boolean debitExecuted,
                                    boolean creditExecuted,
                                    AccountClientResponse sender,
                                    AccountClientResponse receiver,
                                    InternalTransferRequest request,
                                    String transactionId) {
        try {
            if (holdPlaced && !debitExecuted) {
                accountServiceClient.releaseHold(sender.getAccountId(),
                        new HoldRequest(request.getAmount(), transactionId));

            } else if (debitExecuted && !creditExecuted) {
                accountServiceClient.reverseDebit(sender.getAccountId(),
                        new DebitRequest(request.getAmount(), transactionId));

            } else if (debitExecuted && creditExecuted) {
                accountServiceClient.reverseCredit(receiver.getAccountId(),
                        new CreditRequest(request.getAmount(), transactionId));
                accountServiceClient.reverseDebit(sender.getAccountId(),
                        new DebitRequest(request.getAmount(), transactionId));
            }
        } catch (Exception ex) {
            log.error("[TRANSACTION-SERVICE] CRITICAL: Compensation failed for: {}",
                    transactionId, ex);
        }
    }

    private void validateSenderAccount(AccountClientResponse senderClient, InternalTransferRequest request,
                                       Long userId, BigDecimal dailyTransferredAmountSoFar) {

        // Validate sender Account
        if(!Objects.equals(senderClient.getUserId(), userId)){
            log.error("[TRANSACTION-SERVICE] Unauthorized access attempt to transfer funds from account ID: {} by user ID: {}]",
                    request.getSourceAccountId(), userId);
            throw new UnauthorizedException("Unauthorized access attempt");
        }
        if(senderClient.getStatus() == AccountClientResponse.AccountStatus.CLOSED){
            log.error("[TRANSACTION-SERVICE] Account ID: {} is closed", request.getSourceAccountId());
            throw new ValidationException("Your account is closed. Please contact support for assistance");
        }
        if(request.getAmount()
                .compareTo(senderClient.getAvailableBalance()) > 0){
            log.error("[TRANSACTION-SERVICE] Insufficient funds in source account ID: {}",
                    request.getSourceAccountId());
            throw new ValidationException("Insufficient funds in source account");
        }

        // checks if adding THIS transfer would exceed limit
        if(dailyTransferredAmountSoFar
                .add(request.getAmount())
                .compareTo(senderClient.getDailyTransferLimit()) > 0){
            log.error("[TRANSACTION-SERVICE] Daily transfer limit exceeded for account ID: {}. Amount transferred today: {}, Daily limit: {}",
                    senderClient.getAccountId(), dailyTransferredAmountSoFar, senderClient.getDailyTransferLimit());
            throw new ValidationException("Daily transfer limit exceeded");
        }
    }

    private void validateReceiverAccount(AccountClientResponse receiverClient, Currency senderCurrency) {
        if(receiverClient.getStatus() == AccountClientResponse.AccountStatus.CLOSED){
            log.error("[TRANSACTION-SERVICE] Receiver Account ID: {} is closed", receiverClient.getAccountId());
            throw new ValidationException("Receiver account is closed.");
        }
        if (senderCurrency != receiverClient.getCurrency()) {
            throw new ValidationException("Source and destination accounts must use the same currency");
        }
    }
}
