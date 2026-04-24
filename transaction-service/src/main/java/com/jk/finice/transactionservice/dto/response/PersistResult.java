package com.jk.finice.transactionservice.dto.response;

import com.jk.finice.transactionservice.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersistResult {
    private final Transaction transaction;
    private final boolean isOwner; // true = this request won the insert
}
