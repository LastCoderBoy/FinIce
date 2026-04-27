package com.jk.finice.transactionservice.dto.response;


public record IbanValidationResult(
        boolean valid,
        String normalizedIban,
        String countryCode,
        String errorMessage
) {

    public static IbanValidationResult valid(String normalized, String countryCode) {
        return new IbanValidationResult(true, normalized, countryCode, null);
    }

    public static IbanValidationResult invalid(String reason) {
        return new IbanValidationResult(false, null, null, reason);
    }
}
