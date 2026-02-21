package com.jk.finice.accountservice.controller;

import com.jk.finice.accountservice.dto.request.CreateAccountRequest;
import com.jk.finice.accountservice.dto.response.AccountResponse;
import com.jk.finice.accountservice.dto.response.AccountSummaryResponse;
import com.jk.finice.accountservice.service.AccountService;
import com.jk.finice.commonlibrary.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.jk.finice.commonlibrary.constants.AppConstants.ACCOUNT_PATH;
import static com.jk.finice.commonlibrary.constants.AppConstants.USER_ID_HEADER;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ACCOUNT_PATH)
public class AccountController {

    private final AccountService accountService;

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

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> viewAccountDetails(@PathVariable Long accountId,
                                                                           @RequestHeader(USER_ID_HEADER) Long userId ){
        log.info("[ACCOUNT-CONTROLLER] Viewing account details for account ID: {}", accountId);

        AccountResponse accountResponse = accountService.viewAccountDetails(accountId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account details retrieved successfully", accountResponse)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AccountSummaryResponse>> getAccountSummary(@RequestHeader(USER_ID_HEADER) Long userId){
        log.info("[ACCOUNT-CONTROLLER] Getting account summary for user ID: {}", userId);

        AccountSummaryResponse summaryResponse = accountService.getAccountSummary(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account summary retrieved successfully", summaryResponse)
        );
    }
}
