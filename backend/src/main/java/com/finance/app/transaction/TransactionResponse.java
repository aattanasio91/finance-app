package com.finance.app.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID userId,
        UUID accountId,
        UUID categoryId,
        UUID merchantId,
        BigDecimal amount,
        String description,
        LocalDate date,
        TransactionType type,
        String currency,
        boolean isManual,
        boolean isRecurring,
        String notes,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getAccountId(),
                transaction.getCategoryId(),
                transaction.getMerchantId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getDate(),
                transaction.getType(),
                transaction.getCurrency(),
                transaction.isManual(),
                transaction.isRecurring(),
                transaction.getNotes(),
                transaction.getCreatedAt()
        );
    }
}
