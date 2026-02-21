package com.jk.finice.accountservice.util;

public final class MaskingUtils {
    private MaskingUtils() {}

    /**
     * Mask account number (show only last 4 digits)
     * Example: ACC1234567890123 -> ****0123
     */
    public static String maskIban(String iban) {
        if (iban == null || iban.length() < 4) {
            return "****";
        }
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 4);
    }

    /**
     * Mask card number (show first 6 and last 4)
     * Example: 4532123456789012 -> 453212******9012
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return "****";
        }
        return cardNumber.substring(0, 6) + "******" + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Mask email (show first 3 chars and domain)
     * Example: johndoe@gmail.com -> joh***@gmail.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 3) {
            return "***@" + domain;
        }
        return username.substring(0, 3) + "***@" + domain;
    }

    /**
     * Mask phone number (show last 4 digits)
     * Example: +1234567890 -> ****7890
     */
    public static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }

}
