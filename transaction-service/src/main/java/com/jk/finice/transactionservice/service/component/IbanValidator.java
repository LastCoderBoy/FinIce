package com.jk.finice.transactionservice.service.component;

import com.jk.finice.commonlibrary.utils.MaskingUtils;
import com.jk.finice.transactionservice.dto.response.IbanValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Map;

@Component
@Slf4j
public class IbanValidator {

    // Country code -> expected IBAN length
    private static final Map<String, Integer> IBAN_LENGTHS = Map.of(
            "GB", 22,
            "DE", 22,
            "FR", 27,
            "PL", 28,
            "UZ", 24,  // our custom FinIce format
            "TR", 26,
            "US", 24
    );

    /**
     * Full IBAN validation
     * Returns ValidationResult instead of throwing — caller decides how to handle
     */
    public IbanValidationResult validate(String normalizedIban) {
        if (normalizedIban == null || normalizedIban.isBlank()) {
            return IbanValidationResult.invalid("IBAN cannot be empty");
        }

        // Basic format check — only letters and digits
        if (!normalizedIban.matches("^[A-Z0-9]+$")) {
            return IbanValidationResult.invalid("IBAN contains invalid characters");
        }

        // Minimum length
        if (normalizedIban.length() < 5) {
            return IbanValidationResult.invalid("IBAN too short");
        }

        // Extract parts
        String countryCode = normalizedIban.substring(0, 2);

        // Country code must be letters
        if (!countryCode.matches("^[A-Z]{2}$")) {
            return IbanValidationResult.invalid("Invalid country code: " + countryCode);
        }

        // Length check for known countries
        Integer expectedLength = IBAN_LENGTHS.get(countryCode);
        if (expectedLength != null && normalizedIban.length() != expectedLength) {
            return IbanValidationResult.invalid(
                    String.format("Invalid IBAN length for %s. Expected %d, got %d",
                            countryCode, expectedLength, normalizedIban.length())
            );
        }

        // MOD-97 check — the real mathematical validation
        if (!passesMod97(normalizedIban)) {
            return IbanValidationResult.invalid("IBAN check digit validation failed");
        }

        log.debug("[IBAN-VALIDATOR] IBAN validated successfully: {}",
                MaskingUtils.maskIban(normalizedIban));

        return IbanValidationResult.valid(normalizedIban, countryCode);
    }

    /**
     * MOD-97 algorithm — ISO 13616 standard
     * Valid IBAN always produces remainder of 1
     */
    private boolean passesMod97(String iban) {
        try {
            // Rearrange: move first 4 chars to end
            String rearranged = iban.substring(4) + iban.substring(0, 4);

            // Convert letters to numbers (A=10, B=11 ... Z=35)
            StringBuilder numericString = new StringBuilder();
            for (char c : rearranged.toCharArray()) {
                if (Character.isLetter(c)) {
                    numericString.append(Character.getNumericValue(c));
                } else {
                    numericString.append(c);
                }
            }

            // MOD-97 must equal 1 for valid IBAN
            BigInteger numericValue = new BigInteger(numericString.toString());
            return numericValue.remainder(BigInteger.valueOf(97))
                    .equals(BigInteger.ONE);

        } catch (Exception e) {
            log.error("[IBAN-VALIDATOR] MOD-97 calculation failed: {}", e.getMessage());
            return false;
        }
    }
}
