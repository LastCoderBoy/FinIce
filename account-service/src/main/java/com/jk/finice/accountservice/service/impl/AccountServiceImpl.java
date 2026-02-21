package com.jk.finice.accountservice.service.impl;

import com.jk.finice.accountservice.dto.request.CreateAccountRequest;
import com.jk.finice.accountservice.dto.response.AccountResponse;
import com.jk.finice.accountservice.dto.response.AccountSummaryResponse;
import com.jk.finice.accountservice.entity.Account;
import com.jk.finice.accountservice.enums.AccountType;
import com.jk.finice.accountservice.exception.AccountCreationFailedException;
import com.jk.finice.accountservice.repository.AccountRepository;
import com.jk.finice.accountservice.service.AccountService;
import com.jk.finice.accountservice.util.MaskingUtils;
import com.jk.finice.commonlibrary.exception.DatabaseException;
import com.jk.finice.commonlibrary.exception.InternalServerException;
import com.jk.finice.commonlibrary.exception.ResourceNotFoundException;
import com.jk.finice.commonlibrary.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    @Value("${bank.code}")
    private String BANK_CODE;

    @Value("${country.code}")
    private String COUNTRY_CODE;

    private final AccountRepository accountRepository;


    // ==================== OVERRIDDEN METHODS ====================

    // In general, there are 6 accounts per User (1 - Current, 5 - Savings)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AccountResponse createAccount(CreateAccountRequest createRequest, Long userId) {
        try {

            String iban = generateIban();

            if(isAccountTypeValid(createRequest.getAccountType(), userId)){
                Account account = Account.builder()
                        .iban(iban)
                        .userId(userId)
                        .accountType(createRequest.getAccountType())
                        .currency(createRequest.getCurrency())
                        .balance(createRequest.getInitialDeposit())
                        .availableBalance(createRequest.getInitialDeposit()) // Since the account is newly created
                        .accountNickName(createRequest.getNickName())
                        .build();

                accountRepository.save(account);
                accountRepository.flush(); // Populate the timestamps


                log.info("[ACCOUNT-SERVICE] Created account for user: {} with account number: **** {}",
                        userId,
                        MaskingUtils.maskIban(iban));
                return new AccountResponse(account);
            }

            throw new AccountCreationFailedException("Cannot create anymore accounts of " + createRequest.getAccountType() + " type.");

        } catch (AccountCreationFailedException ace) {
            throw ace;
        } catch (Exception e) {
            log.error("[ACCOUNT-SERVICE] Failed to create account for user: {}", userId, e);
            throw new InternalServerException("Failed to create account");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public AccountResponse viewAccountDetails(Long accountId, Long userId) {
        try{
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

            if(!account.getUserId().equals(userId)){
                throw new UnauthorizedException("You are not authorized to view this account.");
            }

            return new AccountResponse(account);

        } catch (ResourceNotFoundException | UnauthorizedException ex){
            throw ex;
        } catch (DataIntegrityViolationException de){
            log.error("[ACCOUNT-SERVICE] Failed to view account details for account ID: {}", accountId, de);
            throw new DatabaseException("Failed to view account details");
        }
        catch (Exception e){
            log.error("[ACCOUNT-SERVICE] Failed to view account details for account ID: {}", accountId, e);
            throw new InternalServerException("Failed to view account details");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public AccountSummaryResponse getAccountSummary(Long userId) {
        try{
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

        } catch (Exception e){
            log.error("[ACCOUNT-SERVICE] Failed to get account summary for user ID: {}", userId, e);
            throw new InternalServerException("Failed to get account summary");
        }
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

        do {
            String accountNumber = generateAccountNumber();
            String bban = BANK_CODE + accountNumber;
            String checkDigit = generateCheckDigit(bban);

            iban = COUNTRY_CODE + checkDigit + bban ;
            attempt++;
        } while (ibanExists(iban) && attempt < maxAttempts);

        if (attempt >= maxAttempts) {
            throw new AccountCreationFailedException("Failed to generate unique IBAN number after " + maxAttempts + " attempts");
        }

        return iban;
    }

    private String generateCheckDigit(String bban) {
        String rearrangedIban = bban + COUNTRY_CODE + "00";

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
}
