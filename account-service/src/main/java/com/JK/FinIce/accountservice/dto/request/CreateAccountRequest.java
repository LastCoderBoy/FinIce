package com.JK.FinIce.accountservice.dto.request;

import com.JK.FinIce.accountservice.enums.AccountType;
import com.JK.FinIce.accountservice.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Initial deposit must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Initial deposit must be a valid decimal number")
    private BigDecimal initialDeposit;

    @Size(max = 100, message = "Account nickname must not exceed 100 characters")
    private String nickName; // Optional: "Emergency Fund", "Vacation Savings", ...
}
