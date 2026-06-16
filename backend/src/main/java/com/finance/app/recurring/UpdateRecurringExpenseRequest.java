package com.finance.app.recurring;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateRecurringExpenseRequest(
        UUID categoryId,
        String name,
        BigDecimal amount,
        Integer dayOfMonth,
        RecurringFrequency frequency,
        Boolean isActive,
        String notes
) {}
