package com.JK.FinIce.accountservice.controller;

import com.JK.FinIce.accountservice.dto.request.CreateAccountRequest;
import com.JK.FinIce.accountservice.dto.response.AccountResponse;
import com.JK.FinIce.accountservice.service.AccountService;
import com.JK.FinIce.commonlibrary.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.ACCOUNT_PATH;
import static com.JK.FinIce.commonlibrary.constants.AppConstants.USER_ID_HEADER;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(ACCOUNT_PATH)
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest createRequest,
            @RequestHeader(USER_ID_HEADER) Long userId ){

        log.info("[ACCOUNT-CONTROLLER] Creating account for user ID: {}", userId);

        AccountResponse accountResponse = accountService.createAccount(createRequest, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account created successfully", accountResponse)
        );
    }
}
