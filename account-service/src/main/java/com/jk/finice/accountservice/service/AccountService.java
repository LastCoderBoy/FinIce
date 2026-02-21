package com.jk.finice.accountservice.service;

import com.jk.finice.accountservice.dto.request.CreateAccountRequest;
import com.jk.finice.accountservice.dto.response.AccountResponse;
import com.jk.finice.accountservice.dto.response.AccountSummaryResponse;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest createRequest, Long userId);

    AccountResponse viewAccountDetails(Long accountId, Long userId);

    AccountSummaryResponse getAccountSummary(Long userId);
}
