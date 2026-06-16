package com.finance.app.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        BigDecimal amount,
        String period,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive
) {
    static BudgetResponse from(Budget budget, String categoryName) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategoryId(),
                categoryName,
                budget.getAmount(),
                budget.getPeriod().name(),
                budget.getStartDate(),
                budget.getEndDate(),
                budget.isActive()
        );
    }
}
