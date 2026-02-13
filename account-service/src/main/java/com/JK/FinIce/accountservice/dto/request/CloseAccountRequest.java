package com.JK.FinIce.accountservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloseAccountRequest {

    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;

    @Size(max = 16, min = 16, message = "Account number must be 16 characters")
    private String transferToAccountNumber;  // Optional: Transfer remaining balance to this account
}
