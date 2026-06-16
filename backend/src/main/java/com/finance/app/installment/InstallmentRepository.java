package com.finance.app.installment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InstallmentRepository extends JpaRepository<Installment, UUID> {

    List<Installment> findByTransactionIdOrderByDueDate(UUID transactionId);

    List<Installment> findByTransactionIdInOrderByDueDate(List<UUID> transactionIds);

    @Query("""
            SELECT i FROM Installment i
            WHERE i.transactionId IN (
                SELECT t.id FROM Transaction t WHERE t.accountId = :accountId
            )
            ORDER BY i.dueDate
            """)
    List<Installment> findByAccountId(@Param("accountId") UUID accountId);

    @Query("""
            SELECT i FROM Installment i
            WHERE i.transactionId IN (
                SELECT t.id FROM Transaction t WHERE t.accountId = :accountId AND t.userId = :userId
            )
            AND i.isPaid = :isPaid
            ORDER BY i.dueDate
            """)
    List<Installment> findByAccountIdAndIsPaid(
            @Param("accountId") UUID accountId,
            @Param("userId") UUID userId,
            @Param("isPaid") boolean isPaid);
}
