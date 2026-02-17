package com.jk.finice.accountservice.dto.response;

import com.jk.finice.accountservice.enums.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

    private String accountNumber;

    /**
     * Total balance in the account
     * This is the actual amount of money you have
     */
    private BigDecimal balance;

    /**
     * Available balance (balance - holds)
     * This is the amount you can withdraw/transfer RIGHT NOW
     */
    private BigDecimal availableBalance;

    /**
     * Amount currently on hold (pending transactions)
     * This money is reserved but not yet deducted
     */
    private BigDecimal holdAmount;

    private Currency currency;
    private LocalDateTime asOfTime;
}
