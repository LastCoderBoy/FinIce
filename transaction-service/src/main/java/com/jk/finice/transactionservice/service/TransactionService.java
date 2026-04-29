package com.jk.finice.transactionservice.service;

import com.jk.finice.commonlibrary.dto.ApiResponse;
import com.jk.finice.commonlibrary.dto.PaginatedResponse;
import com.jk.finice.transactionservice.dto.request.ExternalTransferRequest;
import com.jk.finice.transactionservice.dto.request.InternalTransferRequest;
import com.jk.finice.transactionservice.dto.request.TransactionHistoryFilterRequest;
import com.jk.finice.transactionservice.dto.response.TransactionHistoryItemResponse;
import com.jk.finice.transactionservice.dto.response.TransactionHistoryResponse;
import com.jk.finice.transactionservice.dto.response.TransferResponse;
import jakarta.validation.Valid;

public interface TransactionService {

    PaginatedResponse<TransactionHistoryResponse> getTransactionHistory(TransactionHistoryFilterRequest filterRequest, Long userId);

    TransactionHistoryItemResponse getDetailedHistoryResponse(String transactionId, Long userId);

    TransferResponse internalTransfer(InternalTransferRequest transferRequest, Long userId, String idempotencyKey);

    TransferResponse externalTransfer(ExternalTransferRequest transferRequest, Long userId, String idempotencyKey);
}
