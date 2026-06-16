package com.finance.app.dashboard;

import com.finance.app.account.Account;
import com.finance.app.account.AccountRepository;
import com.finance.app.category.Category;
import com.finance.app.category.CategoryRepository;
import com.finance.app.transaction.TransactionRepository;
import com.finance.app.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public DashboardResponse dashboard(UUID userId) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate yearStart = now.withDayOfYear(1);

        List<Account> accounts = accountRepository.findByUserIdAndIsActiveTrue(userId);
        BigDecimal currentBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthIncome = transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.INCOME, monthStart, monthEnd);
        BigDecimal monthExpenses = transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, monthStart, monthEnd).abs();

        List<DashboardResponse.BalanceByAccount> balanceByAccount = accounts.stream()
                .map(a -> new DashboardResponse.BalanceByAccount(a.getId(), a.getName(), a.getBalance()))
                .toList();

        List<DashboardResponse.ExpenseByCategory> expensesByCategory = getExpensesByCategory(userId, monthStart, monthEnd);

        List<DashboardResponse.MonthlyEvolution> monthlyEvolution = getMonthlyEvolution(userId, yearStart, monthEnd);

        DashboardResponse.Projection projection = calculateProjection(accounts, monthIncome, monthExpenses);

        return new DashboardResponse(
                currentBalance,
                monthIncome,
                monthExpenses,
                monthIncome.subtract(monthExpenses),
                balanceByAccount,
                expensesByCategory,
                monthlyEvolution,
                projection
        );
    }

    public List<DashboardResponse.ExpenseByCategory> expensesByCategory(UUID userId, LocalDate from, LocalDate to) {
        return getExpensesByCategory(userId, from, to);
    }

    public List<DashboardResponse.MonthlyEvolution> monthlyEvolution(UUID userId, LocalDate from, LocalDate to) {
        return getMonthlyEvolution(userId, from, to);
    }

    private List<DashboardResponse.ExpenseByCategory> getExpensesByCategory(UUID userId, LocalDate from, LocalDate to) {
        List<Object[]> results = transactionRepository.sumExpensesByCategory(userId, from, to);

        Map<UUID, BigDecimal> raw = new HashMap<>();
        for (Object[] row : results) {
            UUID catId = (UUID) row[0];
            BigDecimal amount = BigDecimal.valueOf(((Number) row[1]).doubleValue()).abs();
            raw.put(catId, amount);
        }

        BigDecimal total = raw.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardResponse.ExpenseByCategory> list = new ArrayList<>();
        for (var entry : raw.entrySet()) {
            String name = categoryRepository.findById(entry.getKey())
                    .map(Category::getName)
                    .orElse("Unknown");
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            list.add(new DashboardResponse.ExpenseByCategory(name, entry.getValue(), percentage));
        }

        list.sort((a, b) -> b.amount().compareTo(a.amount()));
        return list;
    }

    private List<DashboardResponse.MonthlyEvolution> getMonthlyEvolution(UUID userId, LocalDate from, LocalDate to) {
        List<Object[]> results = transactionRepository.monthlyEvolution(userId, from, to);

        return results.stream().map(row -> {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal income = BigDecimal.valueOf(((Number) row[2]).doubleValue());
            BigDecimal expenses = BigDecimal.valueOf(((Number) row[3]).doubleValue()).abs();
            String label = String.format("%04d-%02d", year, month);
            return new DashboardResponse.MonthlyEvolution(label, income, expenses);
        }).toList();
    }

    private DashboardResponse.Projection calculateProjection(
            List<Account> accounts, BigDecimal monthIncome, BigDecimal monthExpenses) {

        BigDecimal currentBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate now = LocalDate.now();
        int daysPassed = now.getDayOfMonth();
        int daysInMonth = now.lengthOfMonth();
        int daysRemaining = daysInMonth - daysPassed;

        if (daysPassed == 0) {
            return new DashboardResponse.Projection(currentBalance, "POSITIVE");
        }

        BigDecimal dailyNet = (monthIncome.subtract(monthExpenses))
                .divide(BigDecimal.valueOf(daysPassed), 2, RoundingMode.HALF_UP);

        BigDecimal projectedChange = dailyNet.multiply(BigDecimal.valueOf(daysRemaining));
        BigDecimal estimatedEndBalance = currentBalance.add(projectedChange);

        String status;
        if (estimatedEndBalance.compareTo(BigDecimal.ZERO) < 0) {
            status = "NEGATIVE";
        } else if (estimatedEndBalance.compareTo(currentBalance) < 0) {
            status = "CAUTION";
        } else {
            status = "POSITIVE";
        }

        return new DashboardResponse.Projection(estimatedEndBalance, status);
    }
}
