package com.jk.finice.accountservice.enums.converter;

import com.jk.finice.accountservice.enums.AccountType;
import com.jk.finice.commonlibrary.exception.ValidationException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class AccountTypeConverter implements Converter<String, AccountType> {

    @Override
    public AccountType convert(@NonNull String source) {
        try{
            return AccountType.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e){
            String validValues = Arrays.stream(AccountType.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            throw new ValidationException(
                    String.format("Invalid account type value: %s. Valid values are: %s", source, validValues)
            );
        }
    }
}
