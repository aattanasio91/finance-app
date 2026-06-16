package com.finance.app.budget;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetSummaryResponse(
        UUID budgetId,
        UUID categoryId,
        String categoryName,
        BigDecimal budgetAmount,
        BigDecimal spent,
        int percentage,
        String status
) {}
