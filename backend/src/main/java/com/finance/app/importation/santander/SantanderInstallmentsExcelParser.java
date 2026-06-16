package com.finance.app.importation.santander;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SantanderInstallmentsExcelParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern EMBEDDED_ROW = Pattern.compile(
            "(\\d{2}/\\d{2}/\\d{4})\\s*\\n\\s*([A-Za-z0-9*]+(?:[.*]?[A-Za-z0-9]+)*)\\s*\\n\\s*(\\d{8})?\\s*\\n"
    );

    public List<SantanderParsedRow> parse(InputStream inputStream) {
        List<SantanderParsedRow> rows = new ArrayList<>();
        try (POIFSFileSystem fs = new POIFSFileSystem(inputStream);
             Workbook workbook = new HSSFWorkbook(fs)) {

            for (int si = 0; si < workbook.getNumberOfSheets(); si++) {
                Sheet sheet = workbook.getSheetAt(si);
                log.debug("Installments sheet {}: '{}' (lastRow={}, physicalRows={})",
                        si, sheet.getSheetName(), sheet.getLastRowNum(), sheet.getPhysicalNumberOfRows());

                int rowCount = 0;
                boolean headerFound = false;

                for (Row row : sheet) {
                    rowCount++;
                    String firstCell = getCellStringValue(row.getCell(0));

                    if (!headerFound) {
                        if (firstCell != null && (firstCell.equalsIgnoreCase("fecha")
                                || firstCell.toLowerCase().contains("fecha"))) {
                            headerFound = true;
                            log.debug("Installments header at sheet {} row {}: fecha={}", si, rowCount, firstCell);
                            continue;
                        }
                        if (rowCount <= 3) {
                            log.debug("Installments sheet {} row {}: [0]={} [1]={} [2]={} [3]={}",
                                    si, rowCount, firstCell,
                                    getCellStringValue(row.getCell(1)),
                                    getCellStringValue(row.getCell(2)),
                                    getCellStringValue(row.getCell(3)));
                        }
                        continue;
                    }

                    String dateStr = getCellStringValue(row.getCell(0));
                    String description = getCellStringValue(row.getCell(1));
                    String voucher = getCellStringValue(row.getCell(2));
                    String planStr = getCellStringValue(row.getCell(3));
                    String remainingStr = getCellStringValue(row.getCell(4));
                    String amountStr = getCellStringValue(row.getCell(5));

                    if (description == null || description.isBlank()) continue;
                    if (description.toLowerCase().contains("total")) continue;

                    LocalDate date = null;
                    if (dateStr != null && !dateStr.isBlank()) {
                        try { date = LocalDate.parse(dateStr, DATE_FMT); }
                        catch (Exception e) { log.warn("Could not parse date: {}", dateStr); }
                    }

                    Integer totalInstallments = null;
                    if (planStr != null && !planStr.isBlank()) {
                        try { totalInstallments = Integer.parseInt(planStr.trim()); }
                        catch (Exception ignored) {}
                    }

                    Integer remainingInstallments = null;
                    if (remainingStr != null && !remainingStr.isBlank()) {
                        try { remainingInstallments = Integer.parseInt(remainingStr.trim()); }
                        catch (Exception ignored) {}
                    }

                    int currentInst = 1;
                    int totalInst = totalInstallments != null ? totalInstallments : 1;
                    if (remainingInstallments != null && totalInstallments != null) {
                        currentInst = totalInstallments - remainingInstallments + 1;
                    }

                    BigDecimal amount = parseAmount(amountStr);
                    rows.add(new SantanderParsedRow(date, description, amount, "ARS", voucher, currentInst, totalInst, null));
                }
            }

            if (rows.isEmpty()) {
                rows.addAll(parseEmbeddedText(fs));
                if (!rows.isEmpty()) {
                    log.info("Parsed {} rows from embedded text", rows.size());
                }
            }
        } catch (Exception e) {
            log.warn("Standard POI parse failed, trying embedded text: {}", e.getMessage());
            try (POIFSFileSystem fs2 = new POIFSFileSystem(inputStream)) {
                rows.addAll(parseEmbeddedText(fs2));
            } catch (Exception e2) {
                throw new RuntimeException("Failed to parse Santander installments Excel", e);
            }
        }
        return rows;
    }

    private List<SantanderParsedRow> parseEmbeddedText(POIFSFileSystem fs) {
        List<SantanderParsedRow> rows = new ArrayList<>();
        try (HSSFWorkbook wb = new HSSFWorkbook(fs)) {
            ExcelExtractor extractor = new ExcelExtractor(wb);
            extractor.setFormulasNotResults(false);
            String text = extractor.getText();
            log.debug("Installments ExcelExtractor text length={}", text.length());

            String[] lines = text.split("\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                Matcher m = EMBEDDED_ROW.matcher(text);
                if (m.find(i)) {
                    String dateStr = m.group(1);
                    String merchant = m.group(2);
                    String voucher = m.group(3);

                    LocalDate date = null;
                    try { date = LocalDate.parse(dateStr, DATE_FMT); } catch (Exception ignored) {}

                    i = text.indexOf('\n', m.end());
                    if (i < 0) break;

                    rows.add(new SantanderParsedRow(date, merchant,
                            null, "ARS", voucher, null, null, null));
                }
            }

            if (rows.isEmpty()) {
                log.info("Installments: file appears to be a template (ISBAN format) with no extractable rows");
            }
        } catch (Exception e) {
            log.warn("Could not extract embedded text: {}", e.getMessage());
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
                if (val == Math.floor(val)) yield String.valueOf((long) val);
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield String.valueOf(cell.getNumericCellValue()); }
                catch (Exception e) {
                    try { yield cell.getStringCellValue(); }
                    catch (Exception e2) { yield null; }
                }
            }
            default -> null;
        };
    }

    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) return null;
        try {
            String cleaned = amountStr
                    .replace("U$S", "").replace("$", "").replace("ARS", "")
                    .replace(".", "").replace(",", ".").trim();
            if (cleaned.isEmpty()) return null;
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
