package com.finance.app.importation.santander;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SantanderParsedRow(
        LocalDate date,
        String description,
        BigDecimal amount,
        String currency,
        String voucherNumber,
        Integer currentInstallment,
        Integer totalInstallments,
        String rawLine
) {}
