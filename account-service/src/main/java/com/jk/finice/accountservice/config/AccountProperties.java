package com.jk.finice.accountservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
@Configuration
@ConfigurationProperties(prefix = "account")
@Data
public class AccountProperties {

    private Bank bank = new Bank();
    private Country country = new Country();
    private Limits limits = new Limits();

    @Data
    public static class Bank {
        private String code;
    }

    @Data
    public static class Country {
        private String code;
    }

    @Data
    public static class Limits {
        private SavingsLimits savings = new SavingsLimits();
        private CurrentLimits current = new CurrentLimits();
    }

    @Data
    public static class SavingsLimits {
        private int maxAccountsPerUser = 5;
        private BigDecimal dailyWithdrawalLimit = BigDecimal.valueOf(5000);
        private BigDecimal dailyTransferLimit = BigDecimal.valueOf(20000);
        private BigDecimal interestRate = BigDecimal.valueOf(3.5);
    }

    @Data
    public static class CurrentLimits {
        private int maxAccountsPerUser = 1;
        private BigDecimal dailyWithdrawalLimit = BigDecimal.valueOf(50000);
        private BigDecimal dailyTransferLimit = BigDecimal.valueOf(500000);
        private BigDecimal interestRate = BigDecimal.ZERO;
    }
}
