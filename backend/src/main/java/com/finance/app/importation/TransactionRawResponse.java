package com.finance.app.importation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionRawResponse(
        UUID id,
        UUID importJobId,
        String originalDescription,
        String originalAmount,
        String originalDate,
        BigDecimal parsedAmount,
        LocalDate parsedDate,
        String status,
        String errorMessage
) {
    static TransactionRawResponse from(TransactionRaw raw) {
        return new TransactionRawResponse(
                raw.getId(),
                raw.getImportJobId(),
                raw.getOriginalDescription(),
                raw.getOriginalAmount(),
                raw.getOriginalDate(),
                raw.getParsedAmount(),
                raw.getParsedDate(),
                raw.getStatus().name(),
                raw.getErrorMessage()
        );
    }
}
