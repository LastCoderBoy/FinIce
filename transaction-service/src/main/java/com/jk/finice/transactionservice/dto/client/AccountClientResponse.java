package com.jk.finice.transactionservice.dto.client;

import com.jk.finice.commonlibrary.enums.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountClientResponse {

    private Long id;
    private String iban; // optional, might be removed
    private Currency currency;

    // Balance information
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal holdAmount;

    // Account Status
    private AccountStatus status;
    private LocalDateTime closedAt;
    private String closedReason;

    // Limits
    private BigDecimal dailyTransferLimit;
    private BigDecimal dailyWithdrawalLimit;

    // Interest (for savings accounts)
    private BigDecimal interestRate;
    private LocalDateTime lastInterestCalculatedAt;

    // Metadata
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime lastTransactionAt;

    public AccountResponse(Account account) {
        this.id = account.getId();
        this.iban = account.getIban();
        this.accountType = account.getAccountType();
        this.currency = account.getCurrency();
        this.status = account.getStatus();
        this.closedAt = account.getClosedAt();
        this.closedReason = account.getClosedReason();
        this.balance = account.getBalance();
        this.availableBalance = account.getAvailableBalance();
        this.holdAmount = account.getHoldAmount();
        this.dailyTransferLimit = account.getDailyTransferLimit();
        this.dailyWithdrawalLimit = account.getDailyWithdrawalLimit();
        this.interestRate = account.getInterestRate();
        this.lastInterestCalculatedAt = account.getLastInterestCalculatedAt();
        this.nickname = account.getAccountNickName();
        this.createdAt = account.getCreatedAt();
        this.lastTransactionAt = account.getLastTransactionAt();
    }
}
