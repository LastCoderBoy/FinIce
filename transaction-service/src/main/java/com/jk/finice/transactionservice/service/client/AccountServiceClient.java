package com.jk.finice.transactionservice.service.client;

import com.jk.finice.transactionservice.exception.AccountClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import static com.jk.finice.commonlibrary.constants.AppConstants.ACCOUNT_PATH;
import static com.jk.finice.commonlibrary.constants.AppConstants.USER_ID_HEADER;


@Component
@RequiredArgsConstructor
@Slf4j
public class AccountServiceClient {

    private final WebClient.Builder webClientBuilder;
    private static final String ACCOUNT_SERVICE_URL = "lb://account-service";

    /**
     * Get account details by ID
     *
     * @param accountId Account ID
     * @param userId User ID (for ownership check)
     * @return Lightweight account details (AccountDto)
     */
    public AccountDto getAccount(Long accountId, Long userId) {
        log.debug("[ACCOUNT-CLIENT] Fetching account {} for user {}", accountId, userId);

        try {
            // Use ParameterizedTypeReference to preserve generic type
            ApiResponse<AccountDto> response = webClientBuilder.build()
                    .get()
                    .uri(ACCOUNT_SERVICE_URL + ACCOUNT_PATH + "/{accountId}", accountId)
                    .header(USER_ID_HEADER, userId.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        HttpStatus status = HttpStatus.valueOf(clientResponse.statusCode().value());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[ACCOUNT-CLIENT] 4xx error ({}): {}", status.value(), body);
                                    // ✅ Preserve original status code
                                    return Mono.error(new AccountClientException(
                                            "Account error: " + body, status));
                                });
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        log.error("[ACCOUNT-CLIENT] Account service unavailable");
                        // ✅ Return 503 for downstream service failures
                        return Mono.error(new AccountClientException(
                                "Account service unavailable",
                                HttpStatus.SERVICE_UNAVAILABLE));
                    })
                    // ✅ ParameterizedTypeReference preserves ApiResponse<AccountDto>
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<AccountDto>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
                    .block();

            if (response != null && response.isSuccess()) {
                log.debug("[ACCOUNT-CLIENT] Successfully fetched account {}", accountId);
                return response.getData();  // ✅ Type-safe AccountDto
            }

            throw new AccountClientException(
                    "Failed to fetch account: " + accountId,
                    HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (AccountClientException e) {
            throw e;  // Re-throw with preserved status code
        } catch (Exception e) {
            log.error("[ACCOUNT-CLIENT] Unexpected error: {}", e.getMessage());
            throw new AccountClientException(
                    "Failed to communicate with account service",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
