package com.jk.finice.accountservice.service.impl;

import com.jk.finice.accountservice.config.AccountProperties;
import com.jk.finice.accountservice.dto.request.CreateAccountRequest;
import com.jk.finice.accountservice.dto.request.UpdateAccountRequest;
import com.jk.finice.accountservice.dto.response.AccountResponse;
import com.jk.finice.accountservice.dto.response.AccountSettingsResponse;
import com.jk.finice.accountservice.dto.response.AccountSummaryResponse;
import com.jk.finice.accountservice.dto.response.BalanceResponse;
import com.jk.finice.accountservice.entity.Account;
import com.jk.finice.accountservice.enums.AccountType;
import com.jk.finice.accountservice.exception.AccountCreationFailedException;
import com.jk.finice.accountservice.repository.AccountRepository;
import com.jk.finice.accountservice.service.AccountService;
import com.jk.finice.accountservice.util.MaskingUtils;
import com.jk.finice.commonlibrary.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountProperties accountProperties;
    private final AccountRepository accountRepository;


    // ==================== OVERRIDDEN METHODS ====================

    // In general, there are 6 accounts per User (1 - Current, 5 - Savings)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AccountResponse createAccount(CreateAccountRequest createRequest, Long userId) {
        String iban = generateIban();

        if (!isAccountTypeValid(createRequest.getAccountType(), userId)) {
            throw new AccountCreationFailedException(
                    "Cannot create anymore accounts of " + createRequest.getAccountType() + " type."
            );
        }

        Account account = Account.builder()
                .iban(iban)
                .userId(userId)
                .accountType(createRequest.getAccountType())
                .currency(createRequest.getCurrency())
                .balance(createRequest.getInitialDeposit())
                .availableBalance(createRequest.getInitialDeposit())
                .accountNickName(createRequest.getNickName())
                .build();

        accountRepository.save(account);
        accountRepository.flush();

        log.info("[ACCOUNT-SERVICE] Created account for user: {} with IBAN: ****{}",
                userId,
                MaskingUtils.maskIban(iban));

        return new AccountResponse(account);
    }

    @Transactional(readOnly = true)
    @Override
    public AccountResponse viewAccountDetails(Long accountId, Long userId) {
        Account account = getAccountWithOwnership(accountId, userId);
        return new AccountResponse(account);
    }

    @Transactional(readOnly = true)
    @Override
    public BalanceResponse fetchBalance(Long accountId, Long userId){
        Account account = getAccountWithOwnership(accountId, userId);

        return BalanceResponse.builder()
                .iban(MaskingUtils.maskIban(account.getIban()))
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .holdAmount(account.getHoldAmount())
                .currency(account.getCurrency())
                .asOfTime(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public AccountSummaryResponse getAccountSummary(Long userId) {
        List<Account> allAccounts = accountRepository.findAllByUserId(userId);

        int totalSavings = 0, totalCurrent = 0;
        BigDecimal totalBalance = BigDecimal.ZERO,
                totalAvailableBalance = BigDecimal.ZERO,
                totalHoldAmount = BigDecimal.ZERO;

        for(Account account : allAccounts){
            switch (account.getAccountType()){
                case CURRENT:
                    totalCurrent++;
                    break;
                case SAVINGS:
                    totalSavings++;
                    break;
            }
            totalBalance = totalBalance.add(account.getBalance());
            totalAvailableBalance = totalAvailableBalance.add(account.getAvailableBalance());
            totalHoldAmount = totalHoldAmount.add(account.getHoldAmount());
        }

        List<AccountResponse> listOfAccountDto = allAccounts.stream()
                .map(AccountResponse::new)
                .toList();

        return AccountSummaryResponse.builder()
                .totalAccounts(allAccounts.size())
                .savingsAccounts(totalSavings)
                .currentAccounts(totalCurrent)
                .totalBalance(totalBalance)
                .totalAvailableBalance(totalAvailableBalance)
                .totalHoldAmount(totalHoldAmount)
                .accounts(listOfAccountDto)
                .build();
    }

    @Transactional
    @Override
    public AccountSettingsResponse updateAccountSetting(UpdateAccountRequest updateRequest, Long accountId, Long userId){
        Account account = getAccountWithOwnership(accountId, userId);

        // Validate at least one field provided
        if (updateRequest.getDailyWithdrawalLimit() != null) {
            validateWithdrawalLimit(account.getAccountType(), updateRequest.getDailyWithdrawalLimit());
            account.setDailyWithdrawalLimit(updateRequest.getDailyWithdrawalLimit());
        }

        if (updateRequest.getDailyTransferLimit() != null) {
            validateTransferLimit(account.getAccountType(), updateRequest.getDailyTransferLimit());
            account.setDailyTransferLimit(updateRequest.getDailyTransferLimit());
        }


        if(updateRequest.getNickname() != null){
            String trimmedNickname = updateRequest.getNickname().trim();
            if(!trimmedNickname.isEmpty()){
                account.setAccountNickName(updateRequest.getNickname());
            }
        }

        accountRepository.save(account);
        log.info("[ACCOUNT-SERVICE] Updated account settings for user: {} with account number: **** {}",
                userId, MaskingUtils.maskIban(account.getIban()));

        return AccountSettingsResponse.builder()
                .accountNickname(account.getAccountNickName())
                .dailyWithdrawalLimit(account.getDailyWithdrawalLimit())
                .dailyTransferLimit(account.getDailyTransferLimit())
                .updatedAt(account.getUpdatedAt())
                .build();
    }


    // ==================== HELPER METHODS ====================

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

    private String generateIban() {
        String iban;
        int maxAttempts = 5;
        int attempt = 0;
        String bankCode = accountProperties.getBank().getCode();
        String countryCode = accountProperties.getCountry().getCode();

        do {
            String accountNumber = generateAccountNumber();
            String bban = bankCode + accountNumber;
            String checkDigit = generateCheckDigit(bban, countryCode);

            iban = countryCode + checkDigit + bban ;
            attempt++;
        } while (ibanExists(iban) && attempt < maxAttempts);

        if (attempt >= maxAttempts) {
            throw new AccountCreationFailedException("Failed to generate unique IBAN number after " + maxAttempts + " attempts");
        }

        return iban;
    }

    private String generateCheckDigit(String bban, String countryCode) {
        String rearrangedIban = bban + countryCode + "00";

        // Convert letters to numbers (A=10, B=11, ... P=25, L=21 ... Z=35)
        StringBuilder numericString = new StringBuilder();
        for (char c : rearrangedIban.toCharArray()) {
            if (Character.isLetter(c)) {
                numericString.append(Character.getNumericValue(c));
            } else {
                numericString.append(c);
            }
        }

        // Perform Modulo 97 calculation
        BigInteger numericValue = new BigInteger(numericString.toString());
        BigInteger mod97 = numericValue.remainder(new BigInteger("97"));

        // Subtract from 98 to get the check digits
        int checkDigitInt = 98 - mod97.intValue();

        // Format as a 2-digit string (5 becomes "05")
        return String.format("%02d", checkDigitInt);

    }

    private String generateAccountNumber() {
        // Timestamp + Random
        long timestamp = System.currentTimeMillis(); // 13 digits
        int random = ThreadLocalRandom.current().nextInt(0, 1000); // 3 digits

        return String.format("%013d%03d", timestamp, random);
    }

    private boolean ibanExists(String iban) {
        return accountRepository.existsByIban(iban);
    }

    private Account getAccountWithOwnership(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (!account.getUserId().equals(userId)) {
            log.warn("[ACCOUNT-SERVICE] User {} attempted unauthorized access to account {} (owner: {})",
                    userId, accountId, account.getUserId());
            throw new UnauthorizedException("You are not authorized to access this account");
        }

        return account;
    }



    private void validateWithdrawalLimit(AccountType accountType, BigDecimal requestedLimit) {
        BigDecimal maxLimit = getMaxWithdrawalLimit(accountType);

        if (requestedLimit.compareTo(maxLimit) > 0) {
            throw new ValidationException(
                    String.format("Withdrawal limit cannot exceed %s for %s account (requested: %s)",
                            maxLimit, accountType, requestedLimit));
        }
    }

    /**
     * Validate transfer limit
     */
    private void validateTransferLimit(AccountType accountType, BigDecimal requestedLimit) {
        BigDecimal maxLimit = getMaxTransferLimit(accountType);

        if (requestedLimit.compareTo(maxLimit) > 0) {
            throw new ValidationException(
                    String.format("Transfer limit cannot exceed %s for %s account (requested: %s)",
                            maxLimit, accountType, requestedLimit));
        }
    }

    private BigDecimal getMaxTransferLimit(AccountType accountType) {
        return switch (accountType) {
            case SAVINGS -> accountProperties.getLimits().getSavings().getDailyTransferLimit();
            case CURRENT -> accountProperties.getLimits().getCurrent().getDailyTransferLimit();
        };
    }

    private BigDecimal getMaxWithdrawalLimit(AccountType accountType) {
        return switch (accountType) {
            case SAVINGS -> accountProperties.getLimits().getSavings().getDailyWithdrawalLimit();
            case CURRENT -> accountProperties.getLimits().getCurrent().getDailyWithdrawalLimit();
        };
    }
}
