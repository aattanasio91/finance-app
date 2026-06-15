package com.finance.app.transaction;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TransactionSpecification {

    public static Specification<Transaction> byFilters(
            UUID userId, UUID accountId, UUID categoryId, UUID merchantId,
            TransactionType type, LocalDate from, LocalDate to,
            Boolean isManual, Boolean isRecurring) {

        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            predicates.add(cb.equal(root.get("userId"), userId));

            if (accountId != null)
                predicates.add(cb.equal(root.get("accountId"), accountId));
            if (categoryId != null)
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            if (merchantId != null)
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            if (type != null)
                predicates.add(cb.equal(root.get("type"), type));
            if (from != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            if (to != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
            if (isManual != null)
                predicates.add(cb.equal(root.get("isManual"), isManual));
            if (isRecurring != null)
                predicates.add(cb.equal(root.get("isRecurring"), isRecurring));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
