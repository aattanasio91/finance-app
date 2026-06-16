package com.finance.app.importation.parser;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedRow(
        String description,
        BigDecimal amount,
        LocalDate date,
        String rawLine
) {}
