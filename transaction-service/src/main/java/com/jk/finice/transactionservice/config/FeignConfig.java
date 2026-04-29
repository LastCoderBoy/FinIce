package com.jk.finice.transactionservice.config;

import com.jk.finice.commonlibrary.exception.*;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.jk.finice.commonlibrary.constants.AppConstants.SERVICE_KEY_HEADER;

@Configuration
@EnableFeignClients(basePackages = "com.jk.finice.transactionservice.client")
public class FeignConfig {

    @Value("${internal.service.secret}")
    private String internalSecret;

    // Add the internal service secret to all Feign request Headers
    @Bean
    public RequestInterceptor internalServiceInterceptor() {
        return requestTemplate ->
                requestTemplate.header(SERVICE_KEY_HEADER, internalSecret);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 400 -> new ValidationException(
                    "Invalid request!");
            case 401 -> new UnauthorizedException(
                    "Invalid service key");
            case 404 -> new ResourceNotFoundException(
                    "Account not found");
            case 409 -> new AccountClosedException(
                    "Account is closed and cannot be used for transactions");
            default  -> new InternalServerException(
                    "Application Error: " + response.status());
        };
    }
}
