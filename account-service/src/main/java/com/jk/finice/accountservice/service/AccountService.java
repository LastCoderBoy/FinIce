package com.jk.finice.accountservice.service;

import com.jk.finice.accountservice.dto.request.CreateAccountRequest;
import com.jk.finice.accountservice.dto.request.UpdateAccountRequest;
import com.jk.finice.accountservice.dto.response.AccountResponse;
import com.jk.finice.accountservice.dto.response.AccountSettingsResponse;
import com.jk.finice.accountservice.dto.response.AccountSummaryResponse;
import com.jk.finice.accountservice.dto.response.BalanceResponse;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest createRequest, Long userId);

    AccountResponse viewAccountDetails(Long accountId, Long userId);

    AccountSummaryResponse getAccountSummary(Long userId);

    AccountSettingsResponse updateAccountSetting(UpdateAccountRequest updateRequest, Long accountId, Long userId);

    BalanceResponse fetchBalance(Long accountId, Long userId);
}
