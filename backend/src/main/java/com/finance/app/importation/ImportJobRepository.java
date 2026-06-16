package com.finance.app.importation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {
    List<ImportJob> findByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByFileHash(String fileHash);
}
