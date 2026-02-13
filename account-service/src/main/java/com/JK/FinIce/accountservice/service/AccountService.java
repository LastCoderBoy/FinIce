package com.JK.FinIce.accountservice.service;

import com.JK.FinIce.accountservice.dto.request.CreateAccountRequest;
import com.JK.FinIce.accountservice.dto.response.AccountResponse;
import jakarta.validation.Valid;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest createRequest, Long userId);

}
