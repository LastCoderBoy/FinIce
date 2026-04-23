package com.jk.finice.transactionservice.controller;

import com.jk.finice.commonlibrary.dto.ApiResponse;
import com.jk.finice.transactionservice.dto.request.ExternalTransferRequest;
import com.jk.finice.transactionservice.dto.request.InternalTransferRequest;
import com.jk.finice.transactionservice.dto.response.TransferResponse;
import com.jk.finice.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.jk.finice.commonlibrary.constants.AppConstants.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(TRANSACTION_PATH)
public class TransactionController {

    private final TransactionService transactionService;

    // Transfer money between accounts
    @PostMapping("/internal")
    public ResponseEntity<ApiResponse<TransferResponse>> internalTransfer(
            @Valid @RequestBody InternalTransferRequest transferRequest,
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestHeader(name = IDEMPOTENCY_HEADER, required = false) String idempotencyKey){ // required = false because we'll generate one server-side if the client doesn't provide it.


        log.info("[TRANSACTION-CONTROLLER] Internal transfer initiated for user ID: {}", userId);

        TransferResponse response = transactionService.internalTransfer(transferRequest, userId, idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED).body(
                        ApiResponse.success("Transfer successful", response)
                );
    }

    @PostMapping("/external")
    public ResponseEntity<ApiResponse<TransferResponse>> externalTransfer(@Valid @RequestBody ExternalTransferRequest transferRequest,
                                                                          @RequestHeader(USER_ID_HEADER) Long userId){
        log.info("[TRANSACTION-CONTROLLER] External transfer initiated for user ID: {}", userId);

        TransferResponse response = transactionService.externalTransfer(transferRequest, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED).body(
                        ApiResponse.success("Transfer successful", response)
                );
    }

}
