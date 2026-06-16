package com.finance.app.importation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImportJobResponse(
        UUID id,
        String sourceType,
        String fileName,
        String status,
        int totalRows,
        int successRows,
        int errorRows,
        String errorLog,
        LocalDateTime importDate,
        LocalDateTime createdAt,
        List<TransactionRawResponse> raws
) {
    public static ImportJobResponse from(ImportJob job, List<TransactionRaw> raws) {
        return new ImportJobResponse(
                job.getId(),
                job.getSourceType().name(),
                job.getFileName(),
                job.getStatus().name(),
                job.getTotalRows(),
                job.getSuccessRows(),
                job.getErrorRows(),
                job.getErrorLog(),
                job.getImportDate(),
                job.getCreatedAt(),
                raws.stream().map(TransactionRawResponse::from).toList()
        );
    }

    public static ImportJobResponse from(ImportJob job) {
        return new ImportJobResponse(
                job.getId(),
                job.getSourceType().name(),
                job.getFileName(),
                job.getStatus().name(),
                job.getTotalRows(),
                job.getSuccessRows(),
                job.getErrorRows(),
                job.getErrorLog(),
                job.getImportDate(),
                job.getCreatedAt(),
                null
        );
    }
}
