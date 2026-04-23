package com.jk.finice.accountservice.controller;

import com.jk.finice.accountservice.controller.docs.*;
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
import com.jk.finice.accountservice.service.AccountService;
import com.jk.finice.commonlibrary.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jk.finice.commonlibrary.constants.AppConstants.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ACCOUNT_PATH)
@Tag(name = "Accounts", description = "Account management endpoints for FinIce Banking")
public class AccountController {

    private final AccountService accountService;

    @CreateAccountDocs
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest createRequest,
            @RequestHeader(USER_ID_HEADER) Long userId ){ // Token validation was done in the API Gateway

        log.info("[ACCOUNT-CONTROLLER] Creating account for user ID: {}", userId);

        AccountResponse accountResponse = accountService.createAccount(createRequest, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account created successfully", accountResponse)
        );
    }

    @GetAllAccountsDocs
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts(@RequestHeader(USER_ID_HEADER) Long userId ){
        log.info("[ACCOUNT-CONTROLLER] Getting all accounts");

        List<AccountResponse> allAccounts = accountService.getAllAccounts(userId);

        return ResponseEntity.ok(
                ApiResponse.success("All accounts retrieved successfully", allAccounts)
        );
    }

    @GetAccountDetailsDocs
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountDetails(@PathVariable Long accountId,
                                                                           @RequestHeader(USER_ID_HEADER) Long userId ){
        log.info("[ACCOUNT-CONTROLLER] Viewing account details for account ID: {}", accountId);

        AccountResponse accountResponse = accountService.viewAccountDetails(accountId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account details retrieved successfully", accountResponse)
        );
    }

    @GetBalanceDocs
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> fetchBalance(@PathVariable Long accountId,
                                                                     @RequestHeader(USER_ID_HEADER) Long userId){

        log.info("[ACCOUNT-CONTROLLER] Fetching balance for account ID: {}", accountId);

        BalanceResponse balanceResponse = accountService.fetchBalance(accountId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Balance retrieved successfully", balanceResponse)
        );
    }

    @GetSummaryDocs
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AccountSummaryResponse>> getAccountSummary(@RequestHeader(USER_ID_HEADER) Long userId){
        log.info("[ACCOUNT-CONTROLLER] Getting account summary for user ID: {}", userId);

        AccountSummaryResponse summaryResponse = accountService.getAccountSummary(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account summary retrieved successfully", summaryResponse)
        );
    }

    @UpdateSettingsDocs
    @PatchMapping("/{accountId}/settings")
    public ResponseEntity<ApiResponse<AccountSettingsResponse>> updateAccountSetting(@Valid @RequestBody UpdateAccountRequest updateRequest,
                                                                                     @PathVariable Long accountId,
                                                                                     @RequestHeader(USER_ID_HEADER) Long userId){
        log.info("[ACCOUNT-CONTROLLER] Updating account settings for account ID: {}", accountId);

        AccountSettingsResponse settingsResponse = accountService.updateAccountSetting(updateRequest, accountId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account settings updated successfully", settingsResponse)
        );
    }

    // We are not gonna delete the account completely, just close it
    @CloseAccountDocs
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> closeAccount(@PathVariable Long accountId,
                                                           @Valid @RequestBody CloseAccountRequest closeRequest,
                                                           @RequestHeader(USER_ID_HEADER) Long userId){
        log.info("[ACCOUNT-CONTROLLER] Closing account for account ID: {}", accountId);

        accountService.closeAccount(accountId, closeRequest, userId);

        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }


    // ========================================================================
    //                 INTERNAL COMMUNICATION ENDPOINTS
    // ========================================================================


    @GetMapping("/internal/{accountId}")
    public ResponseEntity<AccountInternalResponse> getAccountInternal(
            @PathVariable Long accountId,
            @RequestHeader(SERVICE_KEY_HEADER) String serviceKey ) {
        log.info("[ACCOUNT-CONTROLLER] Internal Service call for Account ID: {}", accountId);

        AccountInternalResponse accountResponse = accountService.getAccountInternal(accountId, serviceKey);

        return ResponseEntity.ok(accountResponse);
    }

    @PutMapping("/internal/{accountId}/hold")
    public void placeHold(@PathVariable Long accountId,
                          @RequestHeader(SERVICE_KEY_HEADER) String serviceKey,
                          @RequestBody HoldRequest request){
        log.info("[ACCOUNT-CONTROLLER] Placing hold on account ID: {}", accountId);

        accountService.placeHold(accountId, serviceKey, request);
    }

    @PutMapping("/internal/{accountId}/debit")
    public void debitAccount(@PathVariable Long accountId,
                             @RequestHeader(SERVICE_KEY_HEADER) String serviceKey,
                             @RequestBody DebitRequest request){
        log.info("[ACCOUNT-CONTROLLER] Debiting account ID: {}", accountId);

        accountService.debitAccount(accountId, serviceKey, request);
    }

    @PutMapping("/internal/{accountId}/credit")
    public void creditAccount(@PathVariable Long accountId,
                              @RequestHeader(SERVICE_KEY_HEADER) String serviceKey,
                              @RequestBody CreditRequest request){
        log.info("[ACCOUNT-CONTROLLER] Crediting account ID: {}", accountId);

        accountService.creditAccount(accountId, serviceKey, request);
    }

    @PutMapping("/internal/{accountId}/release-hold")
    public void releaseHold(@PathVariable Long accountId,
                            @RequestHeader(SERVICE_KEY_HEADER) String serviceKey,
                            @RequestBody HoldRequest request){
        log.info("[ACCOUNT-CONTROLLER] Releasing hold on account ID: {}", accountId);

        accountService.releaseHold(accountId, serviceKey, request);
    }

    @PutMapping("/internal/{accountId}/reverse-debit")
    public void reverseDebit(@PathVariable Long accountId,
                      @RequestHeader(SERVICE_KEY_HEADER) String serviceKey,
                      @RequestBody DebitRequest request){
        log.info("[ACCOUNT-CONTROLLER] Reversing debit on account ID: {}", accountId);

        accountService.reverseDebit(accountId, serviceKey, request);
    }

    @PutMapping("/internal/{accountId}/reverse-credit")
    public void reverseCredit(@PathVariable Long accountId,
                              @RequestHeader(SERVICE_KEY_HEADER) String serviceKey,
                              @RequestBody CreditRequest request){
        log.info("[ACCOUNT-CONTROLLER] Reversing credit on account ID: {}", accountId);

        accountService.reverseCredit(accountId, serviceKey, request);
    }

}
