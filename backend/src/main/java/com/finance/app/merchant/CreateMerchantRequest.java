package com.finance.app.merchant;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateMerchantRequest(
        @NotBlank String name,
        UUID categoryId
) {}
