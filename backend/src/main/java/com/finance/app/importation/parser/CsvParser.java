package com.finance.app.importation.parser;

import com.finance.app.importation.SourceType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public interface CsvParser {
    List<ParsedRow> parse(InputStream inputStream);
    SourceType supportedSource();

    default List<ParsedRow> readAllLines(InputStream inputStream) {
        List<ParsedRow> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(reader)) {

            String header = br.readLine();
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                rows.add(new ParsedRow(trimmed, null, null, trimmed));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV", e);
        }
        return rows;
    }

    default LocalDate parseDate(String dateStr, List<DateTimeFormatter> formatters) {
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(dateStr.trim(), fmt);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    default BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) return null;
        try {
            String cleaned = amountStr.trim()
                    .replace("$", "")
                    .replace("ARS", "")
                    .replace(".", "")
                    .replace(",", ".")
                    .trim();
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
