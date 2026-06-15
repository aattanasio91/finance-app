package com.finance.app.account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID userId,
        String name,
        AccountType type,
        String currency,
        BigDecimal balance,
        boolean isActive
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getName(),
                account.getType(),
                account.getCurrency(),
                account.getBalance(),
                account.isActive()
        );
    }
}
