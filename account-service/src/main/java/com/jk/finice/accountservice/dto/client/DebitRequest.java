package com.jk.finice.accountservice.dto.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DebitRequest {
    private BigDecimal amount;
    private String transactionId; // for audit logging on account-service side
}
