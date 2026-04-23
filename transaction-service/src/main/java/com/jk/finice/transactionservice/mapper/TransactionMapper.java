package com.jk.finice.transactionservice.mapper;

import com.jk.finice.commonlibrary.utils.MaskingUtils;
import com.jk.finice.transactionservice.dto.response.TransferResponse;
import com.jk.finice.transactionservice.entity.Transaction;

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
}
