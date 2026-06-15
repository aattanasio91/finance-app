package com.finance.app.card;

import java.math.BigDecimal;

public record UpdateCardRequest(
        String name,
        Integer closingDay,
        Integer dueDay,
        BigDecimal creditLimit,
        String colorHex
) {}
