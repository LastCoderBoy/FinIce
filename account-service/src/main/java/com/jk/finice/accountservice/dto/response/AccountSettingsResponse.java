package com.jk.finice.accountservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSettingsResponse {
    private String accountNickname;
    private BigDecimal dailyWithdrawalLimit;
    private BigDecimal dailyTransferLimit;
    private LocalDateTime updatedAt;
}
