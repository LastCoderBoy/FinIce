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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.jk.finice.commonlibrary.utils.TokenUtils.generateSecureToken;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountServiceClient accountServiceClient;
    private final TransactionPersistenceService persistenceService;
    private final TransactionRepository transactionRepository;


    @Override
    public TransferResponse internalTransfer(InternalTransferRequest transferRequest, Long userId, String idempotencyKey) {
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

        // Call the account-service client to get the source account details
        AccountClientResponse senderClient =
                accountServiceClient.getAccountInternal(transferRequest.getSourceAccountId());

        validateSenderAccount(
                senderClient, transferRequest, userId,
                persistenceService.getDailyTransferredAmount(
                        senderClient.getIban(), senderClient.getAccountId())
        );


        // Call the account-service client to get the destination account details
        // Throws exception if account not found
        AccountClientResponse receiverClient =
                accountServiceClient.getAccountInternal(transferRequest.getDestinationAccountId());

        validateReceiverAccount(receiverClient, senderClient.getCurrency());


        String transactionId = generateTransactionId();
        String reference = generateReference();

        // Persist PENDING transaction
        Transaction transaction = buildPendingTransaction(
                transferRequest, resolvedKey, transactionId,
                reference, senderClient, receiverClient, userId
        );

        transaction = persistenceService.persistPending(transaction, resolvedKey);


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

            persistenceService.markComplete(transaction);

        } catch (Exception e) {
            log.error("[TRANSACTION-SERVICE] Transfer failed for transaction ID: {}", transactionId, e);

            try {
                if (holdPlaced && !debitExecuted) {
                    accountServiceClient.releaseHold(
                            senderClient.getAccountId(),
                            new HoldRequest(transferRequest.getAmount(), transactionId)
                    );
                } else if (debitExecuted && !creditExecuted) {
                    accountServiceClient.reverseDebit(
                            senderClient.getAccountId(),
                            new DebitRequest(transferRequest.getAmount(), transactionId)
                    );
                } else if (debitExecuted) {
                    accountServiceClient.reverseCredit(
                            receiverClient.getAccountId(),
                            new CreditRequest(transferRequest.getAmount(), transactionId)
                    );
                    accountServiceClient.reverseDebit(
                            senderClient.getAccountId(),
                            new DebitRequest(transferRequest.getAmount(), transactionId)
                    );
                }
            } catch (Exception releaseEx) {
                log.error("[TRANSACTION-SERVICE] Failed compensation for transaction ID: {}",
                        transactionId, releaseEx);
                // flag for manual intervention
            }

            persistenceService.markFailed(transaction, e.getMessage());

            if (e instanceof ValidationException ||
                    e instanceof UnauthorizedException ||
                    e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new TransactionFailedException("Transfer failed: " + e.getMessage(), e);
        }

        return TransactionMapper.toTransferResponse(transaction);
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
        return "TXN-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateReference() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = generateSecureToken().substring(0, 8).toUpperCase();
        return "REF-" + date + "-" + suffix;
    }

    private String resolveKey(String idempotencyKey) {
        return (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();
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
}
