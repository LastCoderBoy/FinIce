package com.jk.finice.transactionservice.specification;

import com.jk.finice.commonlibrary.enums.Currency;
import com.jk.finice.transactionservice.dto.request.TransactionHistoryFilterRequest;
import com.jk.finice.transactionservice.entity.Transaction;
import com.jk.finice.transactionservice.enums.TransactionStatus;
import com.jk.finice.transactionservice.enums.TransactionType;
import com.jk.finice.transactionservice.enums.TransferScope;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionSpecification {

    // Always applied, user can only see their own transactions
    public static Specification<Transaction> belongsToUser(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("createdBy"), userId);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("transactionType"), type);
    }

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> hasCurrency(Currency currency) {
        return (root, query, cb) ->
                currency == null ? null : cb.equal(root.get("currency"), currency);
    }

    public static Specification<Transaction> hasScope(TransferScope scope) {
        return (root, query, cb) ->
                scope == null ? null : cb.equal(root.get("transferScope"), scope);
    }

    public static Specification<Transaction> dateFrom(LocalDate from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(
                        root.get("createdAt"), from.atStartOfDay()
                );
    }

    public static Specification<Transaction> dateTo(LocalDate to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThan(
                        root.get("createdAt"), to.plusDays(1).atStartOfDay()
                );
    }

    public static Specification<Transaction> amountMin(BigDecimal min) {
        return (root, query, cb) ->
                min == null ? null : cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Transaction> amountMax(BigDecimal max) {
        return (root, query, cb) ->
                max == null ? null : cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    /**
     * Compose all filters together
     * null predicates are automatically ignored by Specification.where()
     */
    public static Specification<Transaction> buildFilter(Long userId,
                                                         TransactionHistoryFilterRequest filter) {
        return Specification
                .where(belongsToUser(userId))         // always applied
                .and(hasType(filter.getTransactionType()))
                .and(hasStatus(filter.getStatus()))
                .and(hasScope(filter.getTransferScope()))
                .and(dateFrom(filter.getDateFrom()))
                .and(dateTo(filter.getDateTo()))
                .and(amountMin(filter.getAmountMin()))
                .and(amountMax(filter.getAmountMax()));
    }
}
