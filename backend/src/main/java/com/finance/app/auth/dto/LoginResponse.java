package com.finance.app.auth.dto;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID userId,
        String name,
        String email
) {}
