package com.jk.finice.accountservice.service.impl;

import com.jk.finice.accountservice.dto.request.CreateAccountRequest;
import com.jk.finice.accountservice.dto.response.AccountResponse;
import com.jk.finice.accountservice.entity.Account;
import com.jk.finice.accountservice.enums.AccountType;
import com.jk.finice.accountservice.exception.AccountCreationFailedException;
import com.jk.finice.accountservice.repository.AccountRepository;
import com.jk.finice.accountservice.service.AccountService;
import com.jk.finice.commonlibrary.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;


    // In general, there are 6 accounts per User.
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AccountResponse createAccount(CreateAccountRequest createRequest, Long userId) {
        try {

            String accountNumber = accountNumberGenerator();

            // Current - 1 : Savings up to 5
            if(isAccountTypeValid(createRequest.getAccountType(), userId)){
                Account account = Account.builder()
                        .accountNumber(accountNumber)
                        .userId(userId)
                        .accountType(createRequest.getAccountType())
                        .currency(createRequest.getCurrency())
                        .balance(createRequest.getInitialDeposit())
                        .accountNickName(createRequest.getNickName())
                        .build();

                accountRepository.save(account);
                accountRepository.flush(); // Populate the timestamps


                log.info("[ACCOUNT-SERVICE] Created account for user: {} with account number: {}", userId, accountNumber);
                return new AccountResponse(account);
            }

            throw new AccountCreationFailedException("Account creation failed due to max limit reached.");

        } catch (AccountCreationFailedException ace) {
            throw ace;
        } catch (Exception e) {
            log.error("[ACCOUNT-SERVICE] Failed to create account for user: {}", userId, e);
            throw new InternalServerException("Failed to create account");
        }
    }

    private boolean isAccountTypeValid(AccountType accountType, Long userId) {
        switch (accountType) {
            case CURRENT:
                int totalCurrent = accountRepository.countCurrentAccountTypeForUserId(userId);
                // Current Type should be only 1
                if(totalCurrent > 0){
                    return false;
                }
                break;
            case SAVINGS:
                int totalSavings = accountRepository.countSavingsAccountTypeForUserId(userId);
                // Savings Type should be up to 5
                if(totalSavings > 4){
                    return false;
                }
                break;
        }
        return true;
    }

    private String accountNumberGenerator() {
        String accountNumber;
        int maxAttempts = 5;
        int attempt = 0;

        do {
            accountNumber = generateAccountNumber();
            attempt++;
        } while (accountExists(accountNumber) && attempt < maxAttempts);

        if (attempt >= maxAttempts) {
            throw new AccountCreationFailedException("Failed to generate unique account number after " + maxAttempts + " attempts");
        }

        return accountNumber;
    }

    private String generateAccountNumber() {
        // Timestamp + Random
        long timestamp = System.currentTimeMillis(); // 13 digits
        int random = ThreadLocalRandom.current().nextInt(0, 1000); // 3 digits

        return String.format("%013d%03d", timestamp, random);
    }

    private boolean accountExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }
}
