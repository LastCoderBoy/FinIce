package com.jk.finice.accountservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSummaryResponse {

    private Integer totalAccounts;
    private Integer savingsAccounts;
    private Integer currentAccounts;

    /**
     * Total balance across all accounts
     */
    private BigDecimal totalBalance;

    /**
     * Total available balance across all accounts
     * (what user can actually spend/transfer)
     */
    private BigDecimal totalAvailableBalance;

    /**
     * Total amount on hold across all accounts
     */
    private BigDecimal totalHoldAmount;

    private Currency primaryCurrency;
    private List<AccountResponse> accounts;
}
