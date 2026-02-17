package com.jk.finice.accountservice.service;

import com.jk.finice.accountservice.dto.request.CreateAccountRequest;
import com.jk.finice.accountservice.dto.response.AccountResponse;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest createRequest, Long userId);

}
