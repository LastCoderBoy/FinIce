package com.jk.finice.transactionservice.dto.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HoldRequest {
    private BigDecimal amount;
    private String transactionId;
}
