package com.finance.app.importation.santander;

import com.finance.app.common.exception.BadRequestException;
import com.finance.app.importation.ImportJob;
import com.finance.app.importation.ImportJobRepository;
import com.finance.app.importation.ImportJobResponse;
import com.finance.app.importation.ImportJobStatus;
import com.finance.app.importation.SourceType;
import com.finance.app.installment.Installment;
import com.finance.app.installment.InstallmentRepository;
import com.finance.app.transaction.CreateTransactionRequest;
import com.finance.app.transaction.Transaction;
import com.finance.app.transaction.TransactionRepository;
import com.finance.app.transaction.TransactionResponse;
import com.finance.app.transaction.TransactionService;
import com.finance.app.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SantanderService {

    private final ImportJobRepository importJobRepository;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final InstallmentRepository installmentRepository;

    public ImportJobResponse importBankPdf(UUID userId, UUID accountId, String fileName, InputStream inputStream) {
        SantanderBankPdfParser parser = new SantanderBankPdfParser();
        List<SantanderParsedRow> rows = parser.parse(inputStream);

        ImportJob job = createJob(userId, SourceType.SANTANDER_BANK_PDF, fileName, rows.size());

        int success = 0;
        int errors = 0;
        StringBuilder errorLog = new StringBuilder();

        for (SantanderParsedRow row : rows) {
            try {
                createTransaction(userId, accountId, row);
                success++;
            } catch (Exception e) {
                errorLog.append(row.description()).append(": ").append(e.getMessage()).append("\n");
                errors++;
            }
        }

        return finalizeJob(job, success, errors, errorLog);
    }

    public ImportJobResponse importCardExcel(UUID userId, UUID accountId, String fileName, InputStream inputStream) {
        SantanderCardExcelParser parser = new SantanderCardExcelParser();
        List<SantanderParsedRow> rows = parser.parse(inputStream);

        ImportJob job = createJob(userId, SourceType.SANTANDER_CARD_EXCEL, fileName, rows.size());

        int success = 0;
        int errors = 0;
        StringBuilder errorLog = new StringBuilder();

        for (SantanderParsedRow row : rows) {
            try {
                String description = buildDescription(row);
                createTransaction(userId, accountId, row.date(), description, row.amount(), row.currency());
                success++;
            } catch (Exception e) {
                errorLog.append(row.description()).append(": ").append(e.getMessage()).append("\n");
                errors++;
            }
        }

        return finalizeJob(job, success, errors, errorLog);
    }

    public ImportJobResponse importCardPdf(UUID userId, UUID accountId, String fileName, InputStream inputStream) {
        SantanderCardPdfParser parser = new SantanderCardPdfParser();
        List<SantanderParsedRow> rows = parser.parse(inputStream);

        ImportJob job = createJob(userId, SourceType.SANTANDER_CARD_PDF, fileName, rows.size());

        int success = 0;
        int errors = 0;
        StringBuilder errorLog = new StringBuilder();

        for (SantanderParsedRow row : rows) {
            try {
                String description = buildDescription(row);
                createTransaction(userId, accountId, row.date(), description, row.amount(), row.currency());
                success++;
            } catch (Exception e) {
                errorLog.append(row.description()).append(": ").append(e.getMessage()).append("\n");
                errors++;
            }
        }

        return finalizeJob(job, success, errors, errorLog);
    }

    @Transactional
    public ImportJobResponse importInstallmentsXls(UUID userId, UUID cardId, UUID accountId,
                                                    String fileName, InputStream inputStream) {
        SantanderInstallmentsExcelParser parser = new SantanderInstallmentsExcelParser();
        List<SantanderParsedRow> rows = parser.parse(inputStream);

        ImportJob job = createJob(userId, SourceType.SANTANDER_INSTALLMENTS_XLS, fileName, rows.size());

        int success = 0;
        int errors = 0;
        StringBuilder errorLog = new StringBuilder();

        for (SantanderParsedRow row : rows) {
            try {
                createTransactionAndInstallment(userId, accountId, cardId, row);
                success++;
            } catch (Exception e) {
                errorLog.append(row.description()).append(": ").append(e.getMessage()).append("\n");
                errors++;
            }
        }

        return finalizeJob(job, success, errors, errorLog);
    }

    private void createTransaction(UUID userId, UUID accountId, SantanderParsedRow row) {
        createTransaction(userId, accountId, row.date(), row.description(), row.amount(), row.currency());
    }

    private void createTransaction(UUID userId, UUID accountId, LocalDate date,
                                    String description, BigDecimal amount, String currency) {
        if (amount == null || date == null) {
            throw new BadRequestException("Could not parse amount or date");
        }

        TransactionType type = amount.compareTo(BigDecimal.ZERO) < 0
                ? TransactionType.EXPENSE
                : TransactionType.INCOME;

        CreateTransactionRequest request = new CreateTransactionRequest(
                accountId, null, null, amount.abs(),
                description, date, type,
                currency != null ? currency : "ARS"
        );

        transactionService.create(userId, request);
    }

    private void createTransactionAndInstallment(UUID userId, UUID accountId, UUID cardId, SantanderParsedRow row) {
        if (row.amount() == null || row.date() == null) {
            throw new BadRequestException("Could not parse amount or date");
        }

        CreateTransactionRequest request = new CreateTransactionRequest(
                accountId, null, null, row.amount(),
                row.description(), row.date(), TransactionType.EXPENSE, "ARS"
        );

        TransactionResponse txResponse = transactionService.create(userId, request);
        Transaction tx = transactionRepository.findById(txResponse.id())
                .orElseThrow(() -> new RuntimeException("Transaction not found after creation"));

        int current = row.currentInstallment() != null ? row.currentInstallment() : 1;
        int total = row.totalInstallments() != null ? row.totalInstallments() : 1;

        Installment installment = new Installment();
        installment.setTransactionId(tx.getId());
        installment.setCurrentInstallment(current);
        installment.setTotalInstallments(total);
        installment.setDueDate(row.date());
        installment.setAmount(row.amount());
        installment.setPaid(false);
        installmentRepository.save(installment);
    }

    private String buildDescription(SantanderParsedRow row) {
        StringBuilder sb = new StringBuilder(row.description());
        if (row.currentInstallment() != null && row.totalInstallments() != null) {
            sb.append(" (C.").append(row.currentInstallment()).append("/").append(row.totalInstallments()).append(")");
        }
        return sb.toString();
    }

    private ImportJob createJob(UUID userId, SourceType sourceType, String fileName, int totalRows) {
        ImportJob job = new ImportJob();
        job.setUserId(userId);
        job.setSourceType(sourceType);
        job.setFileName(fileName);
        job.setStatus(ImportJobStatus.PROCESSING);
        job.setTotalRows(totalRows);
        return importJobRepository.save(job);
    }

    private ImportJobResponse finalizeJob(ImportJob job, int success, int errors, StringBuilder errorLog) {
        job.setSuccessRows(success);
        job.setErrorRows(errors);
        job.setErrorLog(errorLog.toString());
        job.setStatus(errors == 0 ? ImportJobStatus.COMPLETED
                : success > 0 ? ImportJobStatus.PARTIAL : ImportJobStatus.FAILED);
        importJobRepository.save(job);
        return ImportJobResponse.from(job);
    }
}
