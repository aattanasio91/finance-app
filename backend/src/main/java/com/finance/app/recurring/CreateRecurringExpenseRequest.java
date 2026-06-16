package com.finance.app.recurring;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRecurringExpenseRequest(
        UUID categoryId,
        @NotBlank String name,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Min(1) @Max(31) int dayOfMonth,
        RecurringFrequency frequency,
        String notes
) {}
