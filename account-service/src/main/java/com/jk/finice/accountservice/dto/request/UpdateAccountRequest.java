package com.jk.finice.accountservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccountRequest {

    @DecimalMin(value = "0.00", message = "Daily transaction limit must be positive")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal dailyTransactionLimit;

    @Size(max = 100, message = "Nickname must not exceed 100 characters")
    private String nickname;
}
