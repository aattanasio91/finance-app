package com.finance.app.account;

import com.finance.app.common.exception.BadRequestException;
import com.finance.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    public List<AccountResponse> findAllByUser(UUID userId) {
        return accountRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(AccountResponse::from)
                .toList();
    }

    public AccountResponse findById(UUID id, UUID userId) {
        return accountRepository.findByIdAndUserId(id, userId)
                .map(AccountResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }

    @Transactional
    public AccountResponse create(UUID userId, CreateAccountRequest request) {
        if (accountRepository.existsByUserIdAndName(userId, request.name())) {
            throw new BadRequestException("Account already exists with name: " + request.name());
        }

        Account account = new Account(userId, request.name(), request.type(), request.currency());
        account.setBalance(request.balance() != null ? request.balance() : java.math.BigDecimal.ZERO);
        account = accountRepository.save(account);
        log.info("Account created: id={}, name={}, type={}, currency={}",
                account.getId(), account.getName(), account.getType(), account.getCurrency());
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse update(UUID id, UUID userId, UpdateAccountRequest request) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        if (request.name() != null) {
            account.setName(request.name());
        }
        if (request.balance() != null) {
            account.setBalance(request.balance());
        }

        account = accountRepository.save(account);
        return AccountResponse.from(account);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        log.info("Account deactivated: id={}, name={}", id, account.getName());
        account.setActive(false);
        accountRepository.save(account);
    }
}
