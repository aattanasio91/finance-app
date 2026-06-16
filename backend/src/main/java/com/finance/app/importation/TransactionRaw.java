package com.finance.app.importation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transaction_raws")
@Getter
@Setter
@NoArgsConstructor
public class TransactionRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "import_job_id", nullable = false)
    private UUID importJobId;

    @Column(name = "original_description", nullable = false, length = 500)
    private String originalDescription;

    @Column(name = "original_amount", length = 100)
    private String originalAmount;

    @Column(name = "original_date", length = 100)
    private String originalDate;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "parsed_amount", precision = 15, scale = 2)
    private BigDecimal parsedAmount;

    @Column(name = "parsed_date")
    private LocalDate parsedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RawStatus status = RawStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
