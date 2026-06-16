package com.finance.app.installment;

import com.finance.app.card.CreditCard;
import com.finance.app.card.CreditCardRepository;
import com.finance.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstallmentService {

    private final InstallmentRepository repository;
    private final CreditCardRepository cardRepository;

    public List<InstallmentResponse> findByCard(UUID cardId, UUID userId) {
        CreditCard card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", cardId));

        return repository.findByAccountId(card.getAccountId()).stream()
                .map(InstallmentResponse::from)
                .toList();
    }

    public List<InstallmentResponse> findByCardAndIsPaid(UUID cardId, UUID userId, boolean isPaid) {
        CreditCard card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", cardId));

        return repository.findByAccountIdAndIsPaid(card.getAccountId(), userId, isPaid).stream()
                .map(InstallmentResponse::from)
                .toList();
    }

    @Transactional
    public void markAsPaid(UUID installmentId) {
        Installment installment = repository.findById(installmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Installment", "id", installmentId));
        installment.setPaid(true);
        installment.setPaidDate(LocalDate.now());
        repository.save(installment);
    }

    public List<InstallmentResponse> findAllByUser(UUID userId) {
        List<UUID> accountIds = cardRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(CreditCard::getAccountId)
                .toList();

        return accountIds.stream()
                .flatMap(accountId -> repository.findByAccountId(accountId).stream())
                .map(InstallmentResponse::from)
                .toList();
    }
}
