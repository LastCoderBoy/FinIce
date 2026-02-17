package com.jk.finice.accountservice.config;

import com.jk.finice.accountservice.enums.converter.AccountTypeConverter;
import com.jk.finice.accountservice.enums.converter.CurrencyConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {

        registry.addConverter(new CurrencyConverter());
        registry.addConverter(new AccountTypeConverter());
    }
}
