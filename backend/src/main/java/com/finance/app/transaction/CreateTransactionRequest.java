package com.finance.app.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull UUID accountId,
        UUID categoryId,
        UUID merchantId,
        @NotNull BigDecimal amount,
        @NotBlank String description,
        @NotNull LocalDate date,
        @NotNull TransactionType type,
        String currency
) {}
