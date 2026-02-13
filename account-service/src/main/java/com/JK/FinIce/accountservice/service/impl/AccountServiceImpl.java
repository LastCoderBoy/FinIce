package com.JK.FinIce.accountservice.service.impl;

import com.JK.FinIce.accountservice.dto.request.CreateAccountRequest;
import com.JK.FinIce.accountservice.dto.response.AccountResponse;
import com.JK.FinIce.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AccountResponse createAccount(CreateAccountRequest createRequest, Long userId) {
        return null;
    }
}
