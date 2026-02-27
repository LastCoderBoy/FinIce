package com.jk.finice.accountservice.dto.request;

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

    @Size(max = 34, min = 24, message = "Account number must be 24 characters at least")
    private String transferToAccountNumber;  // Optional: Transfer remaining balance to this account
}
