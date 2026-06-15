package com.finance.app.user;

public record UpdateUserRequest(
        String name,
        String defaultCurrency
) {}
