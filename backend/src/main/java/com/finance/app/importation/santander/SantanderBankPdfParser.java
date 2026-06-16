package com.finance.app.importation.santander;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SantanderBankPdfParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final Pattern MOVEMENT_LINE = Pattern.compile(
            "(\\d{2}/\\d{2}/\\d{2})\\s+(\\S+)?\\s+(.+?)\\s+([\\-\\$]?[\\d\\.]+,\\d{2})?\\s+([\\-\\$]?[\\d\\.]+,\\d{2})?\\s+([\\-\\$]?[\\d\\.]+,\\d{2})?"
    );
    private static final Pattern SALDO_INICIAL = Pattern.compile("Saldo Inicial");
    private static final Pattern SIMPLE_MOVEMENT = Pattern.compile(
            "(\\d{2}/\\d{2}/\\d{2})\\s+(\\d{8})\\s+(.+?)\\s+((?:-\\$\\s*|\\$\\s*)?[\\d\\.]+,\\d{2})"
    );

    public List<SantanderParsedRow> parse(InputStream inputStream) {
        List<SantanderParsedRow> rows = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            String[] lines = text.split("\\n");
            boolean inMovements = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                if (trimmed.contains("Movimientos en pesos") || trimmed.contains("Movimientos en dólares")) {
                    inMovements = true;
                    continue;
                }

                if (trimmed.contains("Saldo total") && inMovements) {
                    inMovements = false;
                    continue;
                }
                if (trimmed.startsWith("*") || trimmed.startsWith("Banco")) {
                    continue;
                }

                if (!inMovements) continue;

                SantanderParsedRow row = parseLine(trimmed);
                if (row != null) {
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Santander bank PDF", e);
        }
        return rows;
    }

    private SantanderParsedRow parseLine(String line) {
        LocalDate date = null;
        String description = null;
        BigDecimal amount = null;
        String voucher = null;

        Matcher simpleMatcher = SIMPLE_MOVEMENT.matcher(line);
        if (simpleMatcher.find()) {
            try {
                date = LocalDate.parse(simpleMatcher.group(1), DATE_FMT);
            } catch (Exception ignored) {}
            voucher = simpleMatcher.group(2);
            description = simpleMatcher.group(3).trim();
            String amountStr = simpleMatcher.group(4)
                    .replace("$", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", ".");
            try {
                amount = new BigDecimal(amountStr);
            } catch (Exception ignored) {}
        }

        if (description == null && line.contains("Saldo Inicial")) {
            description = "Saldo Inicial";
        }

        if (description == null) return null;

        return new SantanderParsedRow(date, description, amount, "ARS", voucher, null, null, line);
    }
}
