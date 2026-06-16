package com.finance.app.importation.santander;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SantanderBankPdfParserTest {

    private final SantanderBankPdfParser parser = new SantanderBankPdfParser();

    @Test
    void parsesSamplePdf() throws Exception {
        InputStream is = getClass().getResourceAsStream("/santander/bank-pdf-sample.pdf");
        assertNotNull(is, "Sample bank PDF not found");

        List<SantanderParsedRow> rows = parser.parse(is);

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "Should parse at least one row");

        for (SantanderParsedRow row : rows) {
            assertNotNull(row.description(), "Description must not be null");
            assertFalse(row.description().isBlank(), "Description must not be blank");
            assertEquals("ARS", row.currency(), "Currency should be ARS");
        }
    }

    @Test
    void rowsHaveExpectedCount() throws Exception {
        InputStream is = getClass().getResourceAsStream("/santander/bank-pdf-sample.pdf");
        assertNotNull(is);

        List<SantanderParsedRow> rows = parser.parse(is);

        assertTrue(rows.size() >= 20, "Expected at least 20 rows, got " + rows.size());

        boolean hasInitialBalance = rows.stream()
                .anyMatch(r -> r.description().contains("Saldo Inicial"));
        assertTrue(hasInitialBalance, "Should contain Saldo Inicial entry");
    }
}
