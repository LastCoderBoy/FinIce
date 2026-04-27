package com.jk.finice.transactionservice.service.client;


import com.jk.finice.transactionservice.dto.client.AccountClientResponse;
import com.jk.finice.transactionservice.dto.client.CreditRequest;
import com.jk.finice.transactionservice.dto.client.DebitRequest;
import com.jk.finice.transactionservice.dto.client.HoldRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static com.jk.finice.commonlibrary.constants.AppConstants.ACCOUNT_PATH;

@FeignClient(name = "account-service", path = ACCOUNT_PATH)
public interface AccountServiceClient {

    // The Headers are added by the Feign RequestInterceptor Config class for each request.

    @GetMapping("/internal/{accountId}")
    AccountClientResponse getAccountInternal(@PathVariable Long accountId);

    @GetMapping("/internal/by-iban")
    AccountClientResponse getAccountInternalByIban(@RequestParam("iban") String iban);

    @PutMapping("/internal/{accountId}/hold")
    void placeHold(@PathVariable Long accountId,
                   @RequestBody HoldRequest request);

    @PutMapping("/internal/{accountId}/debit")
    void debitAccount(@PathVariable Long accountId,
                      @RequestBody DebitRequest request);

    @PutMapping("/internal/{accountId}/credit")
    void creditAccount(@PathVariable Long accountId,
                       @RequestBody CreditRequest request);

    @PutMapping("/internal/{accountId}/release-hold")
    void releaseHold(@PathVariable Long accountId,
                     @RequestBody HoldRequest request);

    @PutMapping("/internal/{accountId}/reverse-debit")
    void reverseDebit(@PathVariable Long accountId,
                      @RequestBody DebitRequest request);

    @PutMapping("/internal/{accountId}/reverse-credit")
    void reverseCredit(@PathVariable Long accountId,
                       @RequestBody CreditRequest request);
}
