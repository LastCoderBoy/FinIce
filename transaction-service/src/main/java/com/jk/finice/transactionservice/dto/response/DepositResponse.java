package com.jk.finice.transactionservice.dto.response;

import com.jk.finice.commonlibrary.enums.Currency;
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
public class DepositResponse {
    private String transactionId;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private String maskedAccountIban;
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private LocalDateTime completedAt;

    private String failureReason; // might be null (JSON will ignore)
}
