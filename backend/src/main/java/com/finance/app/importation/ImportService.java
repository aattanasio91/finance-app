package com.finance.app.importation;

import com.finance.app.account.Account;
import com.finance.app.account.AccountRepository;
import com.finance.app.common.exception.BadRequestException;
import com.finance.app.common.exception.ResourceNotFoundException;
import com.finance.app.importation.classifier.TransactionClassifier;
import com.finance.app.importation.normalizer.MerchantNormalizer;
import com.finance.app.importation.parser.CsvBankParser;
import com.finance.app.importation.parser.CsvCardParser;
import com.finance.app.importation.parser.CsvParser;
import com.finance.app.importation.parser.ParsedRow;
import com.finance.app.transaction.CreateTransactionRequest;
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
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ImportJobRepository importJobRepository;
    private final TransactionRawRepository transactionRawRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final MerchantNormalizer merchantNormalizer;
    private final TransactionClassifier classifier;

    public List<ImportJobResponse> getHistory(UUID userId) {
        return importJobRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ImportJobResponse::from)
                .toList();
    }

    public ImportJobResponse getDetail(UUID id, UUID userId) {
        ImportJob job = importJobRepository.findById(id)
                .filter(j -> j.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("ImportJob", "id", id));

        List<TransactionRaw> raws = transactionRawRepository.findByImportJobId(id);
        return ImportJobResponse.from(job, raws);
    }

    public List<TransactionRawResponse> getErrors(UUID id, UUID userId) {
        ImportJob job = importJobRepository.findById(id)
                .filter(j -> j.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("ImportJob", "id", id));

        return transactionRawRepository.findByImportJobIdAndStatus(id, RawStatus.ERROR)
                .stream()
                .map(TransactionRawResponse::from)
                .toList();
    }

    @Transactional
    public ImportJobResponse upload(UUID userId, UUID accountId, SourceType sourceType, String fileName, InputStream inputStream) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        byte[] fileBytes = readBytes(inputStream);
        String fileHash = computeHash(fileBytes);

        if (importJobRepository.existsByFileHash(fileHash)) {
            throw new BadRequestException("This file has already been imported");
        }

        CsvParser parser = getParser(sourceType);
        List<ParsedRow> parsedRows = parser.parse(new java.io.ByteArrayInputStream(fileBytes));

        ImportJob job = new ImportJob();
        job.setUserId(userId);
        job.setSourceType(sourceType);
        job.setFileName(fileName);
        job.setFileHash(fileHash);
        job.setStatus(ImportJobStatus.PROCESSING);
        job.setTotalRows(parsedRows.size());
        job = importJobRepository.save(job);

        ImportJob finalJob = job;
        List<TransactionRaw> raws = parsedRows.stream()
                .map(row -> createRaw(finalJob.getId(), row))
                .toList();
        transactionRawRepository.saveAll(raws);

        int success = 0;
        int errors = 0;
        StringBuilder errorLog = new StringBuilder();

        for (TransactionRaw raw : raws) {
            try {
                processRaw(raw, userId, accountId, account.getCurrency());
                raw.setStatus(RawStatus.IMPORTED);
                success++;
            } catch (Exception e) {
                raw.setStatus(RawStatus.ERROR);
                raw.setErrorMessage(e.getMessage());
                errorLog.append("Row ").append(raw.getOriginalDescription()).append(": ").append(e.getMessage()).append("\n");
                errors++;
            }
            transactionRawRepository.save(raw);
        }

        job.setSuccessRows(success);
        job.setErrorRows(errors);
        job.setErrorLog(errorLog.toString());
        job.setStatus(errors == 0 ? ImportJobStatus.COMPLETED
                : success > 0 ? ImportJobStatus.PARTIAL : ImportJobStatus.FAILED);
        importJobRepository.save(job);

        log.info("Import completed: source={}, file={}, total={}, success={}, errors={}",
                sourceType, fileName, parsedRows.size(), success, errors);
        if (errors > 0) {
            log.warn("Import errors:\n{}", errorLog);
        }

        List<TransactionRaw> allRaws = transactionRawRepository.findByImportJobId(job.getId());
        return ImportJobResponse.from(job, allRaws);
    }

    private TransactionRaw createRaw(UUID importJobId, ParsedRow row) {
        TransactionRaw raw = new TransactionRaw();
        raw.setImportJobId(importJobId);
        raw.setOriginalDescription(row.description());
        raw.setOriginalAmount(row.amount() != null ? row.amount().toString() : null);
        raw.setOriginalDate(row.date() != null ? row.date().toString() : null);
        raw.setRawData(row.rawLine());
        raw.setParsedAmount(row.amount());
        raw.setParsedDate(row.date());
        raw.setStatus(RawStatus.PARSED);
        return raw;
    }

    private void processRaw(TransactionRaw raw, UUID userId, UUID accountId, String currency) {
        if (raw.getParsedAmount() == null || raw.getParsedDate() == null) {
            throw new BadRequestException("Could not parse amount or date");
        }

        var merchant = merchantNormalizer.normalize(raw.getOriginalDescription());
        var category = classifier.classify(raw.getOriginalDescription());

        TransactionType type = raw.getParsedAmount().compareTo(BigDecimal.ZERO) < 0
                ? TransactionType.EXPENSE
                : TransactionType.INCOME;

        CreateTransactionRequest request = new CreateTransactionRequest(
                accountId,
                category.map(c -> c.getId()).orElse(null),
                merchant.map(m -> m.getId()).orElse(null),
                raw.getParsedAmount().abs(),
                raw.getOriginalDescription(),
                raw.getParsedDate(),
                type,
                currency
        );

        transactionService.create(userId, request);
    }

    private CsvParser getParser(SourceType sourceType) {
        return switch (sourceType) {
            case CSV_BANK -> new CsvBankParser();
            case CSV_CARD -> new CsvCardParser();
            default -> throw new BadRequestException("Unsupported source type: " + sourceType);
        };
    }

    private byte[] readBytes(InputStream inputStream) {
        try {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    private String computeHash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute file hash", e);
        }
    }
}
