package com.finance.app.budget;

import com.finance.app.category.Category;
import com.finance.app.category.CategoryRepository;
import com.finance.app.common.exception.BadRequestException;
import com.finance.app.common.exception.ResourceNotFoundException;
import com.finance.app.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<BudgetResponse> findAll(UUID userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(b -> BudgetResponse.from(b, resolveCategoryName(b.getCategoryId())))
                .toList();
    }

    public BudgetResponse findById(UUID id, UUID userId) {
        Budget budget = budgetRepository.findById(id)
                .filter(b -> b.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        return BudgetResponse.from(budget, resolveCategoryName(budget.getCategoryId()));
    }

    @Transactional
    public BudgetResponse create(UUID userId, CreateBudgetRequest request) {
        categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        BudgetPeriod period;
        try {
            period = BudgetPeriod.valueOf(request.period().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid period: " + request.period());
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Budget amount must be positive");
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(request.categoryId());
        budget.setAmount(request.amount());
        budget.setPeriod(period);
        budget.setStartDate(request.startDate());
        budget.setEndDate(request.endDate());

        budget = budgetRepository.save(budget);
        return BudgetResponse.from(budget, resolveCategoryName(budget.getCategoryId()));
    }

    @Transactional
    public BudgetResponse update(UUID id, UUID userId, UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .filter(b -> b.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));

        if (request.amount() != null) {
            if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Budget amount must be positive");
            }
            budget.setAmount(request.amount());
        }
        if (request.period() != null) {
            try {
                budget.setPeriod(BudgetPeriod.valueOf(request.period().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid period: " + request.period());
            }
        }
        if (request.startDate() != null) budget.setStartDate(request.startDate());
        if (request.endDate() != null) budget.setEndDate(request.endDate());
        if (request.isActive() != null) budget.setActive(request.isActive());

        budget = budgetRepository.save(budget);
        return BudgetResponse.from(budget, resolveCategoryName(budget.getCategoryId()));
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Budget budget = budgetRepository.findById(id)
                .filter(b -> b.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        budgetRepository.delete(budget);
    }

    public List<BudgetSummaryResponse> getSummary(UUID userId) {
        List<Budget> budgets = budgetRepository.findByUserIdAndIsActiveTrue(userId);
        LocalDate now = LocalDate.now();
        LocalDate periodStart = getPeriodStart(now);
        LocalDate periodEnd = getPeriodEnd(now);

        List<BudgetSummaryResponse> summaries = new ArrayList<>();
        for (Budget budget : budgets) {
            BigDecimal spent = transactionRepository
                    .sumExpensesByUserAndDateBetween(userId, periodStart, periodEnd, budget.getCategoryId())
                    .abs();

            int percentage = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? spent.multiply(BigDecimal.valueOf(100))
                            .divide(budget.getAmount(), 0, RoundingMode.HALF_UP)
                            .intValue()
                    : 0;

            String status;
            if (percentage >= 100) status = "EXCEEDED";
            else if (percentage >= 80) status = "WARNING";
            else status = "OK";

            summaries.add(new BudgetSummaryResponse(
                    budget.getId(),
                    budget.getCategoryId(),
                    resolveCategoryName(budget.getCategoryId()),
                    budget.getAmount(),
                    spent,
                    percentage,
                    status
            ));
        }
        return summaries;
    }

    private LocalDate getPeriodStart(LocalDate now) {
        return now.withDayOfMonth(1);
    }

    private LocalDate getPeriodEnd(LocalDate now) {
        return now.withDayOfMonth(now.lengthOfMonth());
    }

    private String resolveCategoryName(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("Unknown");
    }
}
