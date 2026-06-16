package com.finance.app.recurring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringExpenseResponse(
        UUID id,
        UUID userId,
        UUID categoryId,
        String name,
        BigDecimal amount,
        int dayOfMonth,
        RecurringFrequency frequency,
        boolean isActive,
        LocalDate nextDate,
        String notes,
        LocalDateTime createdAt
) {
    public static RecurringExpenseResponse from(RecurringExpense re) {
        return new RecurringExpenseResponse(
                re.getId(),
                re.getUserId(),
                re.getCategoryId(),
                re.getName(),
                re.getAmount(),
                re.getDayOfMonth(),
                re.getFrequency(),
                re.isActive(),
                re.getNextDate(),
                re.getNotes(),
                re.getCreatedAt()
        );
    }
}
