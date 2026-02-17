package com.jk.finice.accountservice.enums.converter;

import com.jk.finice.accountservice.enums.Currency;
import com.jk.finice.commonlibrary.exception.ValidationException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class CurrencyConverter implements Converter<String, Currency> {

    @Override
    public Currency convert(@NonNull String source) {
        try{
            return Currency.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e){
            String validValues = Arrays.stream(Currency.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            throw new ValidationException(
                    String.format("Invalid currency value: %s. Valid values are: %s", source, validValues)
            );
        }
    }
}
