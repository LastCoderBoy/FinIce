package com.jk.finice.transactionservice.mapper;

import com.jk.finice.commonlibrary.utils.MaskingUtils;
import com.jk.finice.transactionservice.dto.response.TransactionHistoryItemResponse;
import com.jk.finice.transactionservice.dto.response.TransactionHistoryResponse;
import com.jk.finice.transactionservice.dto.response.TransferResponse;
import com.jk.finice.transactionservice.entity.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionMapper {

    public static TransferResponse toTransferResponse(Transaction transaction) {
        return TransferResponse.builder()
                .transactionId(transaction.getTransactionId())
                .reference(transaction.getReference())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getStatus())
                .maskedSenderIban(
                        MaskingUtils.maskIban(transaction.getSenderIban()))
                .maskedReceiverIban(
                        MaskingUtils.maskIban(transaction.getReceiverIban()))
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .completedAt(transaction.getCompletedAt())
                .failureReason(transaction.getFailureReason())
                .build();
    }

    // For history list
    public static TransactionHistoryResponse toHistoryResponse(Transaction t) {
        return TransactionHistoryResponse.builder()
                .transactionId(t.getTransactionId())
                .transactionType(t.getTransactionType())
                .transferScope(t.getTransferScope())
                .status(t.getStatus())
                .maskedSenderIban(
                        MaskingUtils.maskIban(t.getSenderIban()))
                .maskedReceiverIban(
                        MaskingUtils.maskIban(t.getReceiverIban()))
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .createdAt(t.getCreatedAt())
                .build();
    }

    // For single transaction detail
    public static TransactionHistoryItemResponse toHistoryItemResponse(Transaction t) {
        return TransactionHistoryItemResponse.builder()
                .transactionId(t.getTransactionId())
                .reference(t.getReference())
                .transactionType(t.getTransactionType())
                .transferScope(t.getTransferScope())
                .status(t.getStatus())
                .maskedSenderIban(MaskingUtils.maskIban(t.getSenderIban()))
                .maskedReceiverIban(MaskingUtils.maskIban(t.getReceiverIban()))
                .receiverName(t.getReceiverName())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }
}
