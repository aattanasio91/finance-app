package com.finance.app.card;

import com.finance.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CreditCardRepository cardRepository;

    public List<CardResponse> findAllByUser(UUID userId) {
        return cardRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(CardResponse::from)
                .toList();
    }

    public CardResponse findById(UUID id, UUID userId) {
        return cardRepository.findByIdAndUserId(id, userId)
                .map(CardResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", id));
    }

    @Transactional
    public CardResponse create(UUID userId, CreateCardRequest request) {
        CreditCard card = new CreditCard();
        card.setUserId(userId);
        card.setAccountId(request.accountId());
        card.setName(request.name());
        card.setBrand(request.brand());
        card.setClosingDay(request.closingDay());
        card.setDueDay(request.dueDay());
        card.setCreditLimit(request.creditLimit());
        card.setColorHex(request.colorHex());
        card = cardRepository.save(card);
        return CardResponse.from(card);
    }

    @Transactional
    public CardResponse update(UUID id, UUID userId, UpdateCardRequest request) {
        CreditCard card = cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", id));

        if (request.name() != null) card.setName(request.name());
        if (request.closingDay() != null) card.setClosingDay(request.closingDay());
        if (request.dueDay() != null) card.setDueDay(request.dueDay());
        if (request.creditLimit() != null) card.setCreditLimit(request.creditLimit());
        if (request.colorHex() != null) card.setColorHex(request.colorHex());

        card = cardRepository.save(card);
        return CardResponse.from(card);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        CreditCard card = cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", id));
        card.setActive(false);
        cardRepository.save(card);
    }
}
