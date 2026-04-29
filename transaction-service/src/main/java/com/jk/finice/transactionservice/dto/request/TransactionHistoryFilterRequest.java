package com.jk.finice.transactionservice.dto.request;

import com.jk.finice.transactionservice.enums.TransactionStatus;
import com.jk.finice.transactionservice.enums.TransactionType;
import com.jk.finice.transactionservice.enums.TransferScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.jk.finice.commonlibrary.constants.AppConstants.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionHistoryFilterRequest {

    private TransactionType transactionType;
    private TransactionStatus status;
    private TransferScope transferScope;

    private LocalDate dateFrom;
    private LocalDate dateTo;

    private BigDecimal amountMin;
    private BigDecimal amountMax;

    @Builder.Default
    @Min(value = 0, message = "Page must be 0 or greater")
    private int page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    private int size = DEFAULT_PAGE_SIZE;

    @Builder.Default
    @Pattern(regexp = "createdAt|completedAt|currency|receiverName", message = "Invalid sort field. Must be either 'createdAt', 'completedAt', 'currency', or 'receiverName'")
    private String sortBy = DEFAULT_SORT_BY;

    @Builder.Default
    private String sortDirection = DEFAULT_SORT_DIRECTION;

}
