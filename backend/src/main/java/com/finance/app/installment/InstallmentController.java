package com.finance.app.installment;

import com.finance.app.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InstallmentController {

    private final InstallmentService service;

    @GetMapping("/cards/{cardId}/installments")
    public ResponseEntity<ApiResponse<List<InstallmentResponse>>> listByCard(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID cardId,
            @RequestParam(required = false) Boolean isPaid) {
        List<InstallmentResponse> result;
        if (isPaid != null) {
            result = service.findByCardAndIsPaid(cardId, userId, isPaid);
        } else {
            result = service.findByCard(cardId, userId);
        }
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/installments")
    public ResponseEntity<ApiResponse<List<InstallmentResponse>>> listAll(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(service.findAllByUser(userId)));
    }

    @PatchMapping("/installments/{id}/pay")
    public ResponseEntity<ApiResponse<Void>> markAsPaid(@PathVariable UUID id) {
        service.markAsPaid(id);
        return ResponseEntity.ok(ApiResponse.ok("Marked as paid", null));
    }
}
