package com.finance.app.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        BigDecimal currentBalance,
        BigDecimal monthIncome,
        BigDecimal monthExpenses,
        List<BalanceByAccount> balanceByAccount,
        List<ExpenseByCategory> expensesByCategory,
        List<MonthlyEvolution> monthlyEvolution,
        Projection projection
) {
    public record BalanceByAccount(UUID accountId, String name, BigDecimal balance) {}
    public record ExpenseByCategory(String category, BigDecimal amount, BigDecimal percentage) {}
    public record MonthlyEvolution(String month, BigDecimal income, BigDecimal expenses) {}
    public record Projection(BigDecimal estimatedEndBalance, String status) {}
}
