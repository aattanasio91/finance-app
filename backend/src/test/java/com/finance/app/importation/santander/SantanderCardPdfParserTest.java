package com.finance.app.importation.santander;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SantanderCardPdfParserTest {

    private final SantanderCardPdfParser parser = new SantanderCardPdfParser();

    @Test
    void parsesSamplePdf() throws Exception {
        InputStream is = getClass().getResourceAsStream("/santander/card-pdf-sample.pdf");
        assertNotNull(is, "Sample PDF not found");

        List<SantanderParsedRow> rows = parser.parse(is);

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "Should parse at least one row");

        for (SantanderParsedRow row : rows) {
            assertNotNull(row.date(), "Date must not be null");
            assertNotNull(row.description(), "Description must not be null");
            assertFalse(row.description().isBlank(), "Description must not be blank");
            assertNotNull(row.amount(), "Amount must not be null");
            assertTrue(row.amount().compareTo(java.math.BigDecimal.ZERO) > 0,
                    "Amount should be positive (got " + row.amount() + " for " + row.description() + ")");
            assertNotNull(row.currency(), "Currency must not be null");
            assertTrue(row.currency().equals("ARS") || row.currency().equals("USD"),
                    "Currency must be ARS or USD, got: " + row.currency());
        }
    }

    @Test
    void rowsHaveExpectedStructure() throws Exception {
        InputStream is = getClass().getResourceAsStream("/santander/card-pdf-sample.pdf");
        assertNotNull(is);

        List<SantanderParsedRow> rows = parser.parse(is);

        assertTrue(rows.size() >= 10, "Expected at least 10 rows, got " + rows.size());

        SantanderParsedRow first = rows.get(0);
        assertNotNull(first.date());
        assertNotNull(first.description());
        assertNotNull(first.amount());
    }
}
