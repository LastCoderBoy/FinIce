package com.jk.finice.accountservice.controller;

import com.jk.finice.accountservice.dto.request.CreateAccountRequest;
import com.jk.finice.accountservice.dto.response.AccountResponse;
import com.jk.finice.accountservice.service.AccountService;
import com.jk.finice.commonlibrary.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.jk.finice.commonlibrary.constants.AppConstants.ACCOUNT_PATH;
import static com.jk.finice.commonlibrary.constants.AppConstants.USER_ID_HEADER;

@RestController
@Slf4j
@RequestMapping(ACCOUNT_PATH)
public class AccountController {

    private final AccountService accountService;

    // ✅ Add constructor logging
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
        log.info("[ACCOUNT-CONTROLLER] ✅ Controller initialized with mapping: {}", ACCOUNT_PATH);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest createRequest,
            @RequestHeader(USER_ID_HEADER) Long userId ){ // Token validation was done in the API Gateway

        log.info("[ACCOUNT-CONTROLLER] Creating account for user ID: {}", userId);

        AccountResponse accountResponse = accountService.createAccount(createRequest, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account created successfully", accountResponse)
        );
    }
}
