package com.jk.finice.transactionservice.externalGateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ExternalPaymentGateway {

    public ExternalPaymentResult sendPayment(ExternalPaymentRequest request) {
        log.info("[EXTERNAL-GATEWAY] Sending payment to IBAN: {} BIC: {}",
                request.getReceiverIban(), request.getReceiverBic());

        // Simulate network call
        // In real life: HTTP call to SWIFT/SEPA API
        // For FinIce: simulate success/failure

        simulateNetworkDelay();

        return ExternalPaymentResult.builder()
                .success(true)
                .networkReference("SWIFT-" + UUID.randomUUID().toString().substring(0, 8))
                .message("Payment accepted by network")
                .build();
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(500); // simulate 100ms network latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
