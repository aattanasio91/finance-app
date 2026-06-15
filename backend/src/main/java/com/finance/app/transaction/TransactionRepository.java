package com.finance.app.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {

    boolean existsByUserIdAndAmountAndDescriptionAndDateAndAccountId(
            UUID userId, java.math.BigDecimal amount, String description,
            java.time.LocalDate date, UUID accountId);
}
