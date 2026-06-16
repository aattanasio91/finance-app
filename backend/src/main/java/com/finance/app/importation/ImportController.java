package com.finance.app.importation;

import com.finance.app.common.dto.ApiResponse;
import com.finance.app.importation.santander.SantanderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;
    private final SantanderService santanderService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImportJobResponse>> upload(
            @AuthenticationPrincipal UUID userId,
            @RequestParam UUID accountId,
            @RequestParam SourceType sourceType,
            @RequestParam("file") MultipartFile file) throws IOException {

        ImportJobResponse response = importService.upload(
                userId, accountId, sourceType,
                file.getOriginalFilename(),
                file.getInputStream());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping(value = "/santander/bank-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImportJobResponse>> importBankPdf(
            @AuthenticationPrincipal UUID userId,
            @RequestParam UUID accountId,
            @RequestParam("file") MultipartFile file) throws IOException {

        ImportJobResponse response = santanderService.importBankPdf(
                userId, accountId,
                file.getOriginalFilename(),
                file.getInputStream());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping(value = "/santander/card-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImportJobResponse>> importCardPdf(
            @AuthenticationPrincipal UUID userId,
            @RequestParam UUID accountId,
            @RequestParam("file") MultipartFile file) throws IOException {

        ImportJobResponse response = santanderService.importCardPdf(
                userId, accountId,
                file.getOriginalFilename(),
                file.getInputStream());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping(value = "/santander/card-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImportJobResponse>> importCardExcel(
            @AuthenticationPrincipal UUID userId,
            @RequestParam UUID accountId,
            @RequestParam("file") MultipartFile file) throws IOException {

        ImportJobResponse response = santanderService.importCardExcel(
                userId, accountId,
                file.getOriginalFilename(),
                file.getInputStream());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping(value = "/santander/installments-xls", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImportJobResponse>> importInstallmentsXls(
            @AuthenticationPrincipal UUID userId,
            @RequestParam UUID cardId,
            @RequestParam UUID accountId,
            @RequestParam("file") MultipartFile file) throws IOException {

        ImportJobResponse response = santanderService.importInstallmentsXls(
                userId, cardId, accountId,
                file.getOriginalFilename(),
                file.getInputStream());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImportJobResponse>>> list(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(importService.getHistory(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImportJobResponse>> detail(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(importService.getDetail(id, userId)));
    }

    @GetMapping("/{id}/errors")
    public ResponseEntity<ApiResponse<List<TransactionRawResponse>>> errors(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(importService.getErrors(id, userId)));
    }
}
