package com.finance.app.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {

    boolean existsByUserIdAndAmountAndDescriptionAndDateAndAccountId(
            UUID userId, BigDecimal amount, String description,
            LocalDate date, UUID accountId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.userId = :userId AND t.type = :type AND t.date BETWEEN :from AND :to
            """)
    BigDecimal sumByUserAndTypeAndDateBetween(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.userId = :userId AND t.type = 'EXPENSE'
                  AND t.date BETWEEN :from AND :to
                  AND (:categoryId IS NULL OR t.categoryId = :categoryId)
            """)
    BigDecimal sumExpensesByUserAndDateBetween(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("categoryId") UUID categoryId);

    @Query("""
            SELECT t.categoryId, COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.userId = :userId AND t.type = 'EXPENSE'
                  AND t.date BETWEEN :from AND :to
                  AND t.categoryId IS NOT NULL
            GROUP BY t.categoryId
            ORDER BY SUM(t.amount) ASC
            """)
    List<Object[]> sumExpensesByCategory(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT YEAR(t.date), MONTH(t.date),
                   COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0)
            FROM Transaction t
            WHERE t.userId = :userId AND t.date BETWEEN :from AND :to
            GROUP BY YEAR(t.date), MONTH(t.date)
            ORDER BY YEAR(t.date), MONTH(t.date)
            """)
    List<Object[]> monthlyEvolution(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
