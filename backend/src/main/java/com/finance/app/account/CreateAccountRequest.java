package com.finance.app.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank String name,
        @NotNull AccountType type,
        String currency,
        BigDecimal balance
) {}
