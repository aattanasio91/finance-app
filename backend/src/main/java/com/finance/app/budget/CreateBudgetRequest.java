package com.finance.app.budget;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBudgetRequest(
        @NotNull UUID categoryId,
        @NotNull BigDecimal amount,
        @NotBlank String period,
        @NotNull LocalDate startDate,
        LocalDate endDate
) {}
