package com.jk.finice.transactionservice.dto.response;

import com.jk.finice.commonlibrary.enums.Currency;
import com.jk.finice.transactionservice.enums.TransactionStatus;
import com.jk.finice.transactionservice.enums.TransactionType;
import com.jk.finice.transactionservice.enums.TransferScope;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryResponse {

    private String transactionId;      // needed to fetch detail on click
    private TransactionType transactionType;
    private TransferScope transferScope;
    private TransactionStatus status;
    private String maskedSenderIban;
    private String maskedReceiverIban;
    private BigDecimal amount;
    private Currency currency;
    private LocalDateTime createdAt;
}
