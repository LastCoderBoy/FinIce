package com.jk.finice.transactionservice.service;

import com.jk.finice.transactionservice.dto.request.ExternalTransferRequest;
import com.jk.finice.transactionservice.dto.request.InternalTransferRequest;
import com.jk.finice.transactionservice.dto.response.TransferResponse;

public interface TransactionService {
    TransferResponse internalTransfer(InternalTransferRequest transferRequest, Long userId, String idempotencyKey);

    TransferResponse externalTransfer(ExternalTransferRequest transferRequest, Long userId, String idempotencyKey);
}
