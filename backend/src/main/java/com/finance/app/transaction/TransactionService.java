package com.finance.app.transaction;

import com.finance.app.account.Account;
import com.finance.app.account.AccountRepository;
import com.finance.app.common.exception.BadRequestException;
import com.finance.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public Page<TransactionResponse> findAll(
            UUID userId, UUID accountId, UUID categoryId, UUID merchantId,
            TransactionType type, LocalDate from, LocalDate to,
            Boolean isManual, Boolean isRecurring, Pageable pageable) {

        var spec = TransactionSpecification.byFilters(
                userId, accountId, categoryId, merchantId,
                type, from, to, isManual, isRecurring);

        return transactionRepository.findAll(spec, pageable)
                .map(TransactionResponse::from);
    }

    public TransactionResponse findById(UUID id, UUID userId) {
        return transactionRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .map(TransactionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }

    @Transactional
    public TransactionResponse create(UUID userId, CreateTransactionRequest request) {
        Account account = accountRepository.findByIdAndUserId(request.accountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.accountId()));

        if (isDuplicate(userId, request)) {
            log.warn("Duplicate transaction blocked: user={}, desc={}, amount={}, account={}",
                    userId, request.description(), request.amount(), request.accountId());
            throw new BadRequestException("Duplicate transaction");
        }

        validateDate(request.date());
        BigDecimal amount = validateAmount(request.amount(), request.type());

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAccountId(request.accountId());
        transaction.setCategoryId(request.categoryId());
        transaction.setMerchantId(request.merchantId());
        transaction.setAmount(amount);
        transaction.setDescription(request.description());
        transaction.setDate(request.date());
        transaction.setType(request.type());
        transaction.setCurrency(request.currency() != null ? request.currency() : "ARS");
        transaction.setManual(true);

        transaction = transactionRepository.save(transaction);
        updateAccountBalance(account, amount);

        log.info("Transaction created: id={}, userId={}, amount={}, desc={}, type={}",
                transaction.getId(), userId, amount, transaction.getDescription(), transaction.getType());
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public TransactionResponse update(UUID id, UUID userId, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (request.categoryId() != null)
            transaction.setCategoryId(request.categoryId());
        if (request.merchantId() != null)
            transaction.setMerchantId(request.merchantId());
        if (request.description() != null)
            transaction.setDescription(request.description());
        if (request.notes() != null)
            transaction.setNotes(request.notes());

        transaction = transactionRepository.save(transaction);
        log.info("Transaction updated: id={}, description={}", transaction.getId(), transaction.getDescription());
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        Account account = accountRepository.findByIdAndUserId(transaction.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", transaction.getAccountId()));

        account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        accountRepository.save(account);

        log.info("Transaction deleted: id={}, desc={}, amount={}", id, transaction.getDescription(), transaction.getAmount());
        transactionRepository.delete(transaction);
    }

    private boolean isDuplicate(UUID userId, CreateTransactionRequest request) {
        return transactionRepository.existsByUserIdAndAmountAndDescriptionAndDateAndAccountId(
                userId, request.amount(), request.description(), request.date(), request.accountId());
    }

    private void validateDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isAfter(today.plusDays(1)))
            throw new BadRequestException("Transaction date cannot be in the future");
        if (date.isBefore(today.minusYears(5)))
            throw new BadRequestException("Transaction date cannot be more than 5 years ago");
    }

    private BigDecimal validateAmount(BigDecimal amount, TransactionType type) {
        if (amount.compareTo(BigDecimal.ZERO) == 0)
            throw new BadRequestException("Amount must be different from zero");

        if (type == TransactionType.INCOME && amount.compareTo(BigDecimal.ZERO) < 0)
            throw new BadRequestException("Income amount must be positive");

        if (type == TransactionType.EXPENSE && amount.compareTo(BigDecimal.ZERO) > 0)
            return amount.negate();

        return amount;
    }

    private void updateAccountBalance(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }
}
