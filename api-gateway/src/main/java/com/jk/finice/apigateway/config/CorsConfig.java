package com.jk.finice.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static com.jk.finice.commonlibrary.constants.AppConstants.*;
/**
 * Controls which external domains (origins) can make requests to our API from web browsers,
 * preventing unauthorized cross-origin requests while allowing legitimate ones.
 * */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final AppProperties appProperties;

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        corsConfig.setAllowedOrigins(appProperties.getCors().getAllowedOrigins());
        corsConfig.setAllowedMethods(appProperties.getCors().getAllowedMethods());
        corsConfig.setAllowCredentials(appProperties.getCors().isAllowCredentials());
        corsConfig.setMaxAge(appProperties.getCors().getMaxAge());

        corsConfig.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control",
                "Origin"
                // Do NOT include X-User-Id, X-User-Roles here
                // because those are custom headers added by the gateway after authentication, not sent by the browser.
        ));
        corsConfig.setExposedHeaders(Arrays.asList(
                AUTHORIZATION_HEADER,
                USER_ID_HEADER,
                USER_ROLES_HEADER
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
