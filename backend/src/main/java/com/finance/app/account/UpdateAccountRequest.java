package com.finance.app.account;

import java.math.BigDecimal;

public record UpdateAccountRequest(
        String name,
        BigDecimal balance
) {}
