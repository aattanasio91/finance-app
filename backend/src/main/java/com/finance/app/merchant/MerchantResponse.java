package com.finance.app.merchant;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String name,
        String normalizedName,
        UUID categoryId,
        boolean isVerified
) {
    public static MerchantResponse from(Merchant merchant) {
        return new MerchantResponse(
                merchant.getId(),
                merchant.getName(),
                merchant.getNormalizedName(),
                merchant.getCategoryId(),
                merchant.isVerified()
        );
    }
}
