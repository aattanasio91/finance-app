package com.finance.app.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentResponse(
        UUID id,
        UUID transactionId,
        int currentInstallment,
        int totalInstallments,
        LocalDate dueDate,
        BigDecimal amount,
        boolean isPaid,
        LocalDate paidDate
) {
    public static InstallmentResponse from(Installment i) {
        return new InstallmentResponse(
                i.getId(),
                i.getTransactionId(),
                i.getCurrentInstallment(),
                i.getTotalInstallments(),
                i.getDueDate(),
                i.getAmount(),
                i.isPaid(),
                i.getPaidDate()
        );
    }
}
