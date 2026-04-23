package com.jk.finice.transactionservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jk.finice.commonlibrary.enums.Currency;
import com.jk.finice.transactionservice.entity.Transaction;
import com.jk.finice.transactionservice.enums.TransactionStatus;
import com.jk.finice.transactionservice.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferResponse {
    private String transactionId;
    private String reference;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private String maskedSenderIban;
    private String maskedReceiverIban;
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private LocalDateTime completedAt;

    private String failureReason; // might be null (JSON will ignore)
}
