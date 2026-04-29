package com.jk.finice.transactionservice.dto.response;

import com.jk.finice.commonlibrary.enums.Currency;
import com.jk.finice.transactionservice.enums.TransactionStatus;
import com.jk.finice.transactionservice.enums.TransactionType;
import com.jk.finice.transactionservice.enums.TransferScope;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionHistoryItemResponse {
    private String transactionId;
    private String reference;
    private TransactionType transactionType;
    private TransferScope transferScope;
    private TransactionStatus status;

    private String maskedSenderIban;
    private String maskedReceiverIban;
    private String receiverName;

    private BigDecimal amount;
    private Currency currency;
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
