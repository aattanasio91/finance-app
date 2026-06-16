package com.finance.app.recurring;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, UUID> {

    List<RecurringExpense> findByUserIdOrderByName(UUID userId);

    Optional<RecurringExpense> findByIdAndUserId(UUID id, UUID userId);
}
