package com.jk.finice.transactionservice.service.impl;

import com.jk.finice.commonlibrary.dto.PaginatedResponse;
import com.jk.finice.commonlibrary.enums.Currency;
import com.jk.finice.commonlibrary.exception.AccountClosedException;
import com.jk.finice.commonlibrary.exception.ResourceNotFoundException;
import com.jk.finice.commonlibrary.exception.UnauthorizedException;
import com.jk.finice.commonlibrary.exception.ValidationException;
import com.jk.finice.commonlibrary.utils.MaskingUtils;
import com.jk.finice.transactionservice.client.AccountServiceClient;
import com.jk.finice.transactionservice.dto.client.AccountClientResponse;
import com.jk.finice.transactionservice.dto.client.CreditRequest;
import com.jk.finice.transactionservice.dto.client.DebitRequest;
import com.jk.finice.transactionservice.dto.client.HoldRequest;
import com.jk.finice.transactionservice.dto.request.ExternalTransferRequest;
import com.jk.finice.transactionservice.dto.request.InternalTransferRequest;
import com.jk.finice.transactionservice.dto.request.TransactionHistoryFilterRequest;
import com.jk.finice.transactionservice.dto.response.*;
import com.jk.finice.transactionservice.entity.Transaction;
import com.jk.finice.transactionservice.exception.TransactionFailedException;
import com.jk.finice.transactionservice.externalGateway.ExternalPaymentGateway;
import com.jk.finice.transactionservice.mapper.PaginationMapper;
import com.jk.finice.transactionservice.mapper.TransactionMapper;
import com.jk.finice.transactionservice.repository.TransactionRepository;
import com.jk.finice.transactionservice.service.TransactionService;
import com.jk.finice.transactionservice.service.component.IbanValidator;
import com.jk.finice.transactionservice.service.persistence.TransactionPersistenceService;
import com.jk.finice.transactionservice.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final IbanValidator ibanValidator;
    private final AccountServiceClient accountServiceClient;
    private final TransactionPersistenceService persistenceService;
    private final ExternalPaymentGateway externalPaymentGateway;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<TransactionHistoryResponse> getTransactionHistory(TransactionHistoryFilterRequest filterRequest, Long userId){
        int page = filterRequest.getPage();
        int size = filterRequest.getSize();
        String sortDirection = filterRequest.getSortDirection();
        String sortBy = filterRequest.getSortBy();

        // Setup the filter and pagination
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Transaction> spec = TransactionSpecification.buildFilter(userId, filterRequest);

        // Fetch the transactions
        Page<Transaction> pagedTransactions = transactionRepository.findAll(spec, pageable);

        // Map to DTO and return paginated response
        Page<TransactionHistoryResponse> dtoResponse = pagedTransactions.map(TransactionMapper::toHistoryResponse);
        return PaginationMapper.fromPage(dtoResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionHistoryItemResponse getDetailedHistoryResponse(String transactionId, Long userId){
        // Check the transactionID is in valid format.
        // "TXN-" + date + "-" + 12 random;
        validateTransactionId(transactionId); // throws exception if invalid

        Transaction transaction = transactionRepository.findByTransactionIdAndCreatedBy(transactionId, userId)
                .orElseThrow(() -> {
                    log.error("[TRANSACTION-SERVICE] Failed to find transaction with ID: {} for User ID: {}", transactionId, userId);
                    return new ResourceNotFoundException("Transaction not found");
                });

        return TransactionMapper.toHistoryItemResponse(transaction);
    }


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
            if (cause instanceof AccountClosedException ex) throw ex;
            throw new TransactionFailedException(
                    "Failed to fetch account details: " + cause.getMessage(), cause);
        }

        // Validate sender and receiver accounts

        BigDecimal dailyUsed = persistenceService.getDailyTransferredAmount(
                senderClient.getIban(), senderClient.getAccountId()
        );

        validateSenderAccount(senderClient, transferRequest.getSourceAccountId(), transferRequest.getAmount(), userId, dailyUsed);
        validateReceiverAccount(receiverClient, senderClient.getCurrency());


        String transactionId = generateTransactionId();
        String reference = generateReference();

        // Persist PENDING transaction
        Transaction transaction = Transaction.buildPendingInternalTransaction(
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
                    receiverClient, transferRequest.getAmount(), transactionId
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
    public TransferResponse externalTransfer(ExternalTransferRequest transferRequest, Long userId, String idempotencyKey) {

        // Validate receiver IBAN format first before any external calls
        String normalizedReceiverIban = normalizeIban(transferRequest.getReceiverIban());
        IbanValidationResult ibanResult = ibanValidator.validate(normalizedReceiverIban);
        if (!ibanResult.valid()) {
            throw new ValidationException("Invalid receiver IBAN: " + ibanResult.errorMessage());
        }

        // Idempotency check first to fail fast on duplicates before any external calls
        String resolvedKey = resolveKey(idempotencyKey);
        Optional<Transaction> existing = persistenceService.checkIdempotency(resolvedKey);
        if (existing.isPresent()) {
            Transaction existingTxn = existing.get();
            log.info("[TRANSACTION-SERVICE] Duplicate external transfer request detected, returning existing transaction: {}",
                    existingTxn.getTransactionId());
            return TransactionMapper.toTransferResponse(existingTxn);
        }

        // Validate sender account
        AccountClientResponse senderClient = accountServiceClient.getAccountInternal(transferRequest.getSourceAccountId());
        BigDecimal dailyUsed = persistenceService.getDailyTransferredAmount(
                senderClient.getIban(), senderClient.getAccountId()
        );
        validateSenderAccount(
                senderClient, transferRequest.getSourceAccountId(),
                transferRequest.getAmount(), userId, dailyUsed
        );

        // Once we get the sender account, validate the IBAN against the receiver IBAN.
        String normalizedSenderIban = normalizeIban(senderClient.getIban());
        if (normalizedSenderIban.equals(normalizedReceiverIban)) {
            throw new ValidationException("Cannot transfer to the same IBAN");
        }

        try {
            accountServiceClient.getAccountInternalByIban(normalizedReceiverIban);
            // If we reach here - account exists in FinIce
            throw new ValidationException(
                    "Receiver account belongs to FinIce. Please use internal transfer."
            );
        } catch (ResourceNotFoundException e) {
            // Account not in FinIce - continue as external
            log.debug("[TRANSACTION-SERVICE] Receiver IBAN is external, proceeding: {}",
                    MaskingUtils.maskIban(normalizedReceiverIban));
        }

        String transactionId = generateTransactionId();
        String reference = generateReference();
        Transaction transaction = Transaction.buildPendingExternalTransaction(
                transferRequest, resolvedKey, transactionId, reference,
                senderClient, normalizedReceiverIban, userId
        );

        PersistResult persistResult = persistenceService.persistPending(transaction, resolvedKey);
        if (!persistResult.isOwner()) {
            log.info("[TRANSACTION-SERVICE] Concurrent duplicate external transfer detected, returning existing: {}",
                    persistResult.getTransaction().getTransactionId());
            return TransactionMapper.toTransferResponse(persistResult.getTransaction());
        }
        Transaction savedTransaction = persistResult.getTransaction();

        boolean holdPlaced = false;
        boolean debitExecuted = false;

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

            ExternalPaymentGateway.ExternalPaymentResult networkResult = externalPaymentGateway.sendPayment(
                    ExternalPaymentGateway.ExternalPaymentRequest.builder()
                            .receiverIban(normalizedReceiverIban)
                            .build()
            );
            if (!networkResult.isSuccess()) {
                throw new TransactionFailedException("External payment failed: " + networkResult.getMessage());
            }

            savedTransaction.setNetworkReference(networkResult.getNetworkReference());
            persistenceService.markComplete(savedTransaction);
        } catch (Exception e) {
            log.error("[TRANSACTION-SERVICE] External transfer failed for transaction ID: {}", transactionId, e);
            handleCompensation(
                    holdPlaced, debitExecuted, false, senderClient,
                    null, transferRequest.getAmount(), transactionId
            );
            persistenceService.markFailed(savedTransaction, e.getMessage());

            if (e instanceof ValidationException ||
                    e instanceof UnauthorizedException ||
                    e instanceof ResourceNotFoundException ||
                    e instanceof TransactionFailedException){
                throw e;
            }
            throw new TransactionFailedException("External transfer failed: " + e.getMessage(), e);
        }

        return TransactionMapper.toTransferResponse(savedTransaction);
    }


    // =====================================================
    //                     HELPER METHODS
    // =====================================================

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

    private String normalizeIban(String iban) {
        return iban == null ? "" : iban.trim().replace(" ", "").toUpperCase();
    }

    private void handleCompensation(boolean holdPlaced, boolean debitExecuted,
                                    boolean creditExecuted,
                                    AccountClientResponse sender,
                                    AccountClientResponse receiver,
                                    BigDecimal amount,
                                    String transactionId) {
        try {
            if (holdPlaced && !debitExecuted) {
                accountServiceClient.releaseHold(sender.getAccountId(),
                        new HoldRequest(amount, transactionId));

            } else if (debitExecuted && !creditExecuted) {
                accountServiceClient.reverseDebit(sender.getAccountId(),
                        new DebitRequest(amount, transactionId));

            } else if (debitExecuted && creditExecuted) {
                accountServiceClient.reverseCredit(receiver.getAccountId(),
                        new CreditRequest(amount, transactionId));
                accountServiceClient.reverseDebit(sender.getAccountId(),
                        new DebitRequest(amount, transactionId));
            }
        } catch (Exception ex) {
            log.error("[TRANSACTION-SERVICE] CRITICAL: Compensation failed for: {}",
                    transactionId, ex);
        }
    }

    private void validateSenderAccount(AccountClientResponse senderClient, Long sourceAccountId, BigDecimal amount,
                                       Long userId, BigDecimal dailyTransferredAmountSoFar) {

        // Validate sender Account
        if(!Objects.equals(senderClient.getUserId(), userId)){
            log.error("[TRANSACTION-SERVICE] Unauthorized access attempt to transfer funds from account ID: {} by user ID: {}]",
                    sourceAccountId, userId);
            throw new UnauthorizedException("Unauthorized access attempt");
        }

        if(amount.compareTo(senderClient.getAvailableBalance()) > 0){
            log.error("[TRANSACTION-SERVICE] Insufficient funds in source account ID: {}",
                    sourceAccountId);
            throw new ValidationException("Insufficient funds in source account");
        }

        // checks if adding THIS transfer would exceed limit
        if(dailyTransferredAmountSoFar
                .add(amount)
                .compareTo(senderClient.getDailyTransferLimit()) > 0){
            log.error("[TRANSACTION-SERVICE] Daily transfer limit exceeded for account ID: {}. Amount transferred today: {}, Daily limit: {}",
                    senderClient.getAccountId(), dailyTransferredAmountSoFar, senderClient.getDailyTransferLimit());
            throw new ValidationException("Daily transfer limit exceeded");
        }
    }

    private void validateReceiverAccount(AccountClientResponse receiverClient, Currency senderCurrency) {
        if (senderCurrency != receiverClient.getCurrency()) {
            throw new ValidationException("Source and destination accounts must use the same currency");
        }
    }

    private void validateTransactionId(String transactionId) {
        String[] splitTransaction = transactionId.trim().split("-");
        if(splitTransaction.length != 3 ||
                !splitTransaction[0].equalsIgnoreCase("TXN") ||
                splitTransaction[1].length() != 8 || splitTransaction[2].length() != 12){
            throw new ValidationException("Invalid transaction ID format");
        }
    }
}
