package com.JK.FinIce.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.*;
/**
 * Controls which external domains (origins) can make requests to our API from web browsers,
 * preventing unauthorized cross-origin requests while allowing legitimate ones.
 * */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed.origins}")
    private String[] allowedOrigins;

    @Value("${cors.allowed.methods}")
    private String[] allowedMethods;

    @Value("${cors.allow.credentials}")
    private boolean allowCredentials;

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        corsConfiguration.setAllowedMethods(Arrays.asList(allowedMethods));
        corsConfiguration.setAllowCredentials(allowCredentials);
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setMaxAge(3600L); // caching for 1 hour

        corsConfiguration.setAllowedHeaders(
                Arrays.asList(USER_ROLES_HEADER, AUTHORIZATION_HEADER, USER_ID_HEADER, "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
}
