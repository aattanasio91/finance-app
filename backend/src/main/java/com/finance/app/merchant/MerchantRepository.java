package com.finance.app.merchant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByNormalizedName(String normalizedName);

    boolean existsByNormalizedName(String normalizedName);
}
