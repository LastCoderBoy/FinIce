package com.jk.finice.accountservice.dto.client;

import com.jk.finice.accountservice.entity.Account;
import com.jk.finice.accountservice.enums.AccountStatus;
import com.jk.finice.commonlibrary.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountInternalResponse {

    private Long accountId;
    private Long userId;
    private String iban;
    private AccountStatus status;
    private Currency currency;
    private BigDecimal availableBalance;
    private BigDecimal dailyTransferLimit;
    private BigDecimal dailyWithdrawalLimit;

    public AccountInternalResponse(Account account) {
        this.accountId = account.getId();
        this.userId = account.getUserId();
        this.iban = account.getIban();
        this.status = account.getStatus();
        this.currency = account.getCurrency();
        this.availableBalance = account.getAvailableBalance();
        this.dailyTransferLimit = account.getDailyTransferLimit();
        this.dailyWithdrawalLimit = account.getDailyWithdrawalLimit();
    }
}
