package com.jk.finice.transactionservice.dto.response;

@Getter
@AllArgsConstructor
public class IbanValidationResult {
    private final boolean valid;
    private final String normalizedIban;  // spaces removed, uppercased
    private final String countryCode;
    private final String errorMessage;

    public static IbanValidationResult valid(String normalized, String countryCode) {
        return new IbanValidationResult(true, normalized, countryCode, null);
    }

    public static IbanValidationResult invalid(String reason) {
        return new IbanValidationResult(false, null, null, reason);
    }
}
