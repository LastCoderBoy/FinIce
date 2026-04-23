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

    private Long accountId;
    private Long userId;
    private String iban;
    private AccountStatus status;
    private Currency currency;
    private BigDecimal availableBalance;
    private BigDecimal dailyTransferLimit;
    private BigDecimal dailyWithdrawalLimit;


    public enum AccountStatus {
        ACTIVE,
        CLOSED
    }
}
