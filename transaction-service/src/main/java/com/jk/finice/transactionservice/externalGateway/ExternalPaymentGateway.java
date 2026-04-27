package com.jk.finice.transactionservice.externalGateway;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ExternalPaymentGateway {

    public ExternalPaymentResult sendPayment(ExternalPaymentRequest request) {
        log.info("[EXTERNAL-GATEWAY] Sending payment to IBAN: {}", request.getReceiverIban());

        // Simulate network call
        // In real life: HTTP call to SWIFT/SEPA API
        // For FinIce: we trigger success/failure

        simulateNetworkDelay();

        if (shouldFailDeterministically(request.getReceiverIban())) {
            return ExternalPaymentResult.builder()
                    .success(false)
                    .networkReference(null)
                    .message("Payment rejected by network simulator")
                    .build();
        }

        return ExternalPaymentResult.builder()
                .success(true)
                .networkReference("SWIFT-" + UUID.randomUUID().toString().substring(0, 8))
                .message("Payment accepted by network")
                .build();
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(200); // simulate 200ms network latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Deterministic failure rule:
     * Use last IBAN character signal and fail when signal % 5 == 0 (~20%).
     * This makes simulator behavior repeatable for the same IBAN.
     */
    private boolean shouldFailDeterministically(String receiverIban) {
        String normalized = receiverIban == null
                ? ""
                : receiverIban.trim().replace(" ", "").toUpperCase();

        if (normalized.isEmpty()) {
            return true;
        }

        char tail = normalized.charAt(normalized.length() - 1);
        int signal;

        if (Character.isDigit(tail)) {
            signal = tail - '0';
        } else if (tail >= 'A' && tail <= 'Z') {
            signal = 10 + (tail - 'A');
        } else {
            signal = 0;
        }

        return signal % 5 == 0;
    }

    @Builder
    @Getter
    @Setter
    public static class ExternalPaymentResult {
        private boolean success;
        private String networkReference;
        private String message;
    }

    @Getter
    @Setter
    @Builder
    public static class ExternalPaymentRequest {
        private String receiverIban;
    }

}
