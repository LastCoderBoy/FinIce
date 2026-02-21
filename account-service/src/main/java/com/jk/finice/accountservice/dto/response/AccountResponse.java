package com.jk.finice.accountservice.dto.response;

import com.jk.finice.accountservice.entity.Account;
import com.jk.finice.accountservice.enums.AccountType;
import com.jk.finice.accountservice.enums.Currency;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    private Long id;
    private String iban;
    private AccountType accountType;
    private Currency currency;

    // Balance information
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal holdAmount;

    // Limits
    private BigDecimal dailyTransactionLimit;

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
        this.balance = account.getBalance();
        this.availableBalance = account.getAvailableBalance();
        this.holdAmount = account.getHoldAmount();
        this.dailyTransactionLimit = account.getDailyTransactionLimit();
        this.interestRate = account.getInterestRate();
        this.lastInterestCalculatedAt = account.getLastInterestCalculatedAt();
        this.nickname = account.getAccountNickName();
        this.createdAt = account.getCreatedAt();
        this.lastTransactionAt = account.getLastTransactionAt();
    }
}
