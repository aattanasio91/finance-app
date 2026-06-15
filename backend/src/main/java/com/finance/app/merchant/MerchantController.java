package com.finance.app.merchant;

import com.finance.app.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MerchantResponse>>> list() {
        List<MerchantResponse> merchants = merchantService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(merchants));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MerchantResponse>> create(
            @Valid @RequestBody CreateMerchantRequest request) {
        MerchantResponse response = merchantService.create(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMerchantRequest request) {
        MerchantResponse response = merchantService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
