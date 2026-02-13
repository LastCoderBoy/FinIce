package com.JK.FinIce.accountservice.dto.response;

import com.JK.FinIce.accountservice.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private Currency currency;

    // Balance information
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal holdAmount;

    // Limits
    private BigDecimal dailyWithdrawalLimit;
    private BigDecimal dailyTransferLimit;

    // Interest (for savings accounts)
    private BigDecimal interestRate;
    private LocalDateTime lastInterestCalculatedAt;

    // Metadata
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime lastTransactionAt;
}
