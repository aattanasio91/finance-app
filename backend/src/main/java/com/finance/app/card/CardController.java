package com.finance.app.card;

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
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CardResponse>>> list(@AuthenticationPrincipal UUID userId) {
        List<CardResponse> cards = cardService.findAllByUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(cards));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CardResponse>> get(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        CardResponse response = cardService.findById(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CardResponse>> create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateCardRequest request) {
        CardResponse response = cardService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CardResponse>> update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCardRequest request) {
        CardResponse response = cardService.update(id, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        cardService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Card deleted", null));
    }
}
