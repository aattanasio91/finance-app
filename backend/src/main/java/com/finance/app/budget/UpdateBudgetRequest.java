package com.finance.app.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateBudgetRequest(
        BigDecimal amount,
        String period,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isActive
) {}
