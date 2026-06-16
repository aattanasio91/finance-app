package com.finance.app.importation.parser;

import com.finance.app.importation.SourceType;
import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvCardParser implements CsvParser {

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    @Override
    public List<ParsedRow> parse(InputStream inputStream) {
        List<ParsedRow> rows = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] header = reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 3) continue;
                String rawLine = String.join(",", line);

                String dateStr = line[0].trim();
                String description = line[1].trim();
                String amountStr = line[2].trim();

                LocalDate date = parseDate(dateStr, FORMATTERS);
                BigDecimal amount = parseAmount(amountStr);

                rows.add(new ParsedRow(description, amount, date, rawLine));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse card CSV", e);
        }
        return rows;
    }

    @Override
    public SourceType supportedSource() {
        return SourceType.CSV_CARD;
    }
}
