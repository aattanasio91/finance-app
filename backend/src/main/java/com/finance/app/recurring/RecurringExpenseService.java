package com.finance.app.recurring;

import com.finance.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final RecurringExpenseRepository repository;

    public List<RecurringExpenseResponse> findAllByUser(UUID userId) {
        return repository.findByUserIdOrderByName(userId).stream()
                .map(RecurringExpenseResponse::from)
                .toList();
    }

    public RecurringExpenseResponse findById(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId)
                .map(RecurringExpenseResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringExpense", "id", id));
    }

    @Transactional
    public RecurringExpenseResponse create(UUID userId, CreateRecurringExpenseRequest request) {
        RecurringExpense re = new RecurringExpense();
        re.setUserId(userId);
        re.setCategoryId(request.categoryId());
        re.setName(request.name());
        re.setAmount(request.amount());
        re.setDayOfMonth(request.dayOfMonth());
        re.setFrequency(request.frequency() != null ? request.frequency() : RecurringFrequency.MONTHLY);
        re.setNotes(request.notes());
        re.setNextDate(computeNextDate(request.dayOfMonth(), re.getFrequency()));
        re = repository.save(re);
        return RecurringExpenseResponse.from(re);
    }

    @Transactional
    public RecurringExpenseResponse update(UUID id, UUID userId, UpdateRecurringExpenseRequest request) {
        RecurringExpense re = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringExpense", "id", id));

        if (request.categoryId() != null) re.setCategoryId(request.categoryId());
        if (request.name() != null) re.setName(request.name());
        if (request.amount() != null) re.setAmount(request.amount());
        if (request.dayOfMonth() != null) {
            re.setDayOfMonth(request.dayOfMonth());
            re.setNextDate(computeNextDate(request.dayOfMonth(), re.getFrequency()));
        }
        if (request.frequency() != null) {
            re.setFrequency(request.frequency());
            re.setNextDate(computeNextDate(re.getDayOfMonth(), request.frequency()));
        }
        if (request.isActive() != null) re.setActive(request.isActive());
        if (request.notes() != null) re.setNotes(request.notes());

        re = repository.save(re);
        return RecurringExpenseResponse.from(re);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        RecurringExpense re = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringExpense", "id", id));
        repository.delete(re);
    }

    public LocalDate computeNextDate(int dayOfMonth, RecurringFrequency frequency) {
        LocalDate today = LocalDate.now();
        LocalDate candidate;
        if (dayOfMonth > today.lengthOfMonth()) {
            candidate = today.withDayOfMonth(today.lengthOfMonth());
        } else {
            candidate = today.withDayOfMonth(dayOfMonth);
        }
        if (candidate.isBefore(today) || candidate.isEqual(today)) {
            candidate = switch (frequency) {
                case MONTHLY -> candidate.plusMonths(1);
                case BIMONTHLY -> candidate.plusMonths(2);
                case QUARTERLY -> candidate.plusMonths(3);
                case YEARLY -> candidate.plusYears(1);
            };
            int maxDay = candidate.lengthOfMonth();
            if (dayOfMonth > maxDay) {
                candidate = candidate.withDayOfMonth(maxDay);
            }
        }
        return candidate;
    }
}
