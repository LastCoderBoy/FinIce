package com.jk.finice.accountservice.service;

import com.jk.finice.accountservice.dto.client.AccountInternalResponse;
import com.jk.finice.accountservice.dto.client.CreditRequest;
import com.jk.finice.accountservice.dto.client.DebitRequest;
import com.jk.finice.accountservice.dto.client.HoldRequest;
import com.jk.finice.accountservice.dto.request.CloseAccountRequest;
import com.jk.finice.accountservice.dto.request.CreateAccountRequest;
import com.jk.finice.accountservice.dto.request.UpdateAccountRequest;
import com.jk.finice.accountservice.dto.response.AccountResponse;
import com.jk.finice.accountservice.dto.response.AccountSettingsResponse;
import com.jk.finice.accountservice.dto.response.AccountSummaryResponse;
import com.jk.finice.accountservice.dto.response.BalanceResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest createRequest, Long userId);

    AccountResponse viewAccountDetails(Long accountId, Long userId);

    List<AccountResponse> getAllAccounts(Long userId);

    AccountSummaryResponse getAccountSummary(Long userId);

    AccountSettingsResponse updateAccountSetting(UpdateAccountRequest updateRequest, Long accountId, Long userId);

    BalanceResponse fetchBalance(Long accountId, Long userId);

    void closeAccount(Long accountId, CloseAccountRequest closeAccountRequest, Long userId);

    AccountInternalResponse getAccountInternal(Long accountId, String serviceKey);

    AccountInternalResponse getAccountInternalByIban(String iban, String serviceKey);

    void placeHold(Long accountId, String serviceKey, HoldRequest request);

    void debitAccount(Long accountId, String serviceKey, DebitRequest request);

    void creditAccount(Long accountId, String serviceKey, CreditRequest request);

    void releaseHold(Long accountId, String serviceKey, HoldRequest request);

    void reverseDebit(Long accountId, String serviceKey, DebitRequest request);

    void reverseCredit(Long accountId, String serviceKey, CreditRequest request);
}
