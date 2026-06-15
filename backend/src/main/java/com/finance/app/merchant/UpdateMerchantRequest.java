package com.finance.app.merchant;

import java.util.UUID;

public record UpdateMerchantRequest(
        String name,
        UUID categoryId
) {}
