package com.jk.finice.transactionservice.dto.request;

import com.jk.finice.commonlibrary.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest {
    @NotNull(message = "Source account ID is required")
    private Long sourceAccountId;

    @NotNull(message = "Destination account ID is required")
    private Long destinationAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least $0.01")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
