package com.finance.app.recurring;

import com.finance.app.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurring-expenses")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final RecurringExpenseService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringExpenseResponse>>> list(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(service.findAllByUser(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> get(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id, userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> create(
            @AuthenticationPrincipal UUID userId, @Valid @RequestBody CreateRecurringExpenseRequest request) {
        var response = service.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> update(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID id,
            @Valid @RequestBody UpdateRecurringExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        service.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
