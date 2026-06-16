package com.finance.app.importation.santander;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SantanderCardExcelParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<SantanderParsedRow> parse(InputStream inputStream) {
        List<SantanderParsedRow> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean inDataSection = false;
            boolean headerFound = false;

            for (Row row : sheet) {
                String firstCell = getCellStringValue(row.getCell(0));

                if (firstCell != null && firstCell.equals("Fecha")) {
                    headerFound = true;
                    inDataSection = true;
                    continue;
                }

                if (!inDataSection) continue;

                String dateStr = getCellStringValue(row.getCell(0));
                String description = getCellStringValue(row.getCell(1));
                String cuotasStr = getCellStringValue(row.getCell(2));
                String voucher = getCellStringValue(row.getCell(3));
                String pesoAmount = getCellStringValue(row.getCell(4));
                String usdAmount = getCellStringValue(row.getCell(5));

                if (description == null || description.isBlank()) continue;
                if (description.equals("Subtotal") || description.contains("Subtotal")) break;

                String amountStr = usdAmount != null && !usdAmount.isBlank() && !usdAmount.equals("0,00")
                        ? usdAmount
                        : (pesoAmount != null && !pesoAmount.isBlank() ? pesoAmount : null);

                if (amountStr == null) {
                    log.warn("Skipping row with no amount: {}", description);
                    continue;
                }

                String currency = usdAmount != null && !usdAmount.isBlank() && !usdAmount.equals("0,00")
                        ? "USD" : "ARS";

                BigDecimal amount = parseAmount(amountStr);
                if (amount == null) {
                    log.warn("Could not parse amount for: {} raw={}", description, amountStr);
                    continue;
                }

                if (dateStr == null || dateStr.isBlank()) {
                    log.warn("Skipping row with no date: {}", description);
                    continue;
                }
                LocalDate date;
                try {
                    date = LocalDate.parse(dateStr, DATE_FMT);
                } catch (Exception e) {
                    log.warn("Could not parse date for: {} raw={}", description, dateStr);
                    continue;
                }

                Integer currentInst = null;
                Integer totalInst = null;
                if (cuotasStr != null && !cuotasStr.isBlank()) {
                    String[] parts = cuotasStr.split(" de ");
                    try {
                        currentInst = Integer.parseInt(parts[0].trim());
                        if (parts.length > 1) {
                            totalInst = Integer.parseInt(parts[1].trim());
                        }
                    } catch (Exception ignored) {}
                }

                rows.add(new SantanderParsedRow(
                        date, description, amount, currency,
                        voucher, currentInst, totalInst, null
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Santander card Excel", e);
        }
        return rows;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        yield cell.getStringCellValue();
                    } catch (Exception e2) {
                        yield null;
                    }
                }
            }
            default -> null;
        };
    }

    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) return null;
        try {
            String cleaned = amountStr
                    .replace("U$S", "")
                    .replace("$", "")
                    .replace("ARS", "")
                    .replace(".", "")
                    .replace(",", ".")
                    .trim();
            if (cleaned.isEmpty()) return null;
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
