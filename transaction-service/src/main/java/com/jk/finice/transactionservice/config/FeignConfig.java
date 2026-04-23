package com.jk.finice.transactionservice.config;

import com.jk.finice.commonlibrary.exception.InternalServerException;
import com.jk.finice.commonlibrary.exception.ResourceNotFoundException;
import com.jk.finice.commonlibrary.exception.UnauthorizedException;
import com.jk.finice.commonlibrary.exception.ValidationException;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.jk.finice.commonlibrary.constants.AppConstants.SERVICE_KEY_HEADER;

@Configuration
@EnableFeignClients(basePackages = "com.jk.finice.transactionservice.service.client")
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
            case 404 -> new ResourceNotFoundException(
                    "Account not found");
            case 401 -> new UnauthorizedException(
                    "Invalid service key");
            case 400 -> new ValidationException(
                    "Bad request to account-service");
            default  -> new InternalServerException(
                    "account-service error: " + response.status());
        };
    }
}
