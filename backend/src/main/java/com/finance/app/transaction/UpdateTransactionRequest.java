package com.finance.app.transaction;

import java.util.UUID;

public record UpdateTransactionRequest(
        UUID categoryId,
        UUID merchantId,
        String description,
        String notes
) {}
