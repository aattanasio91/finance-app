package com.finance.app.importation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRawRepository extends JpaRepository<TransactionRaw, UUID> {
    List<TransactionRaw> findByImportJobId(UUID importJobId);
    List<TransactionRaw> findByImportJobIdAndStatus(UUID importJobId, RawStatus status);
    long countByImportJobIdAndStatus(UUID importJobId, RawStatus status);
}
