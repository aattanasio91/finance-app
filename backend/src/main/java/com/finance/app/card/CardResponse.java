package com.finance.app.card;

import java.math.BigDecimal;
import java.util.UUID;

public record CardResponse(
        UUID id,
        UUID userId,
        UUID accountId,
        String name,
        CardBrand brand,
        int closingDay,
        int dueDay,
        BigDecimal creditLimit,
        String colorHex,
        boolean isActive
) {
    public static CardResponse from(CreditCard card) {
        return new CardResponse(
                card.getId(),
                card.getUserId(),
                card.getAccountId(),
                card.getName(),
                card.getBrand(),
                card.getClosingDay(),
                card.getDueDay(),
                card.getCreditLimit(),
                card.getColorHex(),
                card.isActive()
        );
    }
}
