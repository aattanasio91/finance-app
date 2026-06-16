package com.finance.app.importation.santander;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SantanderInstallmentsExcelParserTest {

    private final SantanderInstallmentsExcelParser parser = new SantanderInstallmentsExcelParser();

    private InputStream createWorkbook(RowWriter writer) throws Exception {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Fecha");
            header.createCell(1).setCellValue("Descripcion");
            header.createCell(2).setCellValue("Voucher");
            header.createCell(3).setCellValue("Plan");
            header.createCell(4).setCellValue("Remaining");
            header.createCell(5).setCellValue("Amount");
            writer.write(sheet);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }

    @FunctionalInterface
    interface RowWriter {
        void write(Sheet sheet) throws Exception;
    }

    @Test
    void parsesStandardRow() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("15/05/2026");
            r1.createCell(1).setCellValue("MERCADO LIBRE");
            r1.createCell(2).setCellValue("12345678");
            r1.createCell(3).setCellValue("12");
            r1.createCell(4).setCellValue("9");
            r1.createCell(5).setCellValue("2.500,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertEquals(1, rows.size());
        SantanderParsedRow row = rows.get(0);
        assertEquals(LocalDate.of(2026, 5, 15), row.date());
        assertEquals("MERCADO LIBRE", row.description());
        assertEquals(Integer.valueOf(4), row.currentInstallment());
        assertEquals(Integer.valueOf(12), row.totalInstallments());
        assertEquals(0, new BigDecimal("2500.00").compareTo(row.amount()));
        assertEquals("ARS", row.currency());
    }

    @Test
    void parsesFirstInstallment() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("01/06/2026");
            r1.createCell(1).setCellValue("SPOTIFY");
            r1.createCell(3).setCellValue("6");
            r1.createCell(4).setCellValue("6");
            r1.createCell(5).setCellValue("1.000,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertEquals(1, rows.size());
        assertEquals(Integer.valueOf(1), rows.get(0).currentInstallment());
        assertEquals(Integer.valueOf(6), rows.get(0).totalInstallments());
    }

    @Test
    void parsesLastInstallment() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("01/06/2026");
            r1.createCell(1).setCellValue("DISNEY+");
            r1.createCell(3).setCellValue("12");
            r1.createCell(4).setCellValue("1");
            r1.createCell(5).setCellValue("500,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertEquals(1, rows.size());
        assertEquals(Integer.valueOf(12), rows.get(0).currentInstallment());
        assertEquals(Integer.valueOf(12), rows.get(0).totalInstallments());
    }

    @Test
    void skipsTotalRow() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("15/05/2026");
            r1.createCell(1).setCellValue("NETFLIX");
            r1.createCell(5).setCellValue("1.500,00");
            Row r2 = sheet.createRow(2);
            r2.createCell(1).setCellValue("Total");
            r2.createCell(5).setCellValue("1.500,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertEquals(1, rows.size());
    }

    @Test
    void skipsRowWithNullDescription() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("15/05/2026");
            r1.createCell(5).setCellValue("1.000,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertTrue(rows.isEmpty());
    }

    @Test
    void handlesMissingOptionalFields() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(1).setCellValue("BASIC SERVICE");
            r1.createCell(5).setCellValue("1.000,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertEquals(1, rows.size());
        assertNull(rows.get(0).date());
        assertEquals(Integer.valueOf(1), rows.get(0).currentInstallment());
        assertEquals(Integer.valueOf(1), rows.get(0).totalInstallments());
    }

    @Test
    void emptySheetReturnsNoRows() throws Exception {
        InputStream is = createWorkbook(sheet -> {});
        List<SantanderParsedRow> rows = parser.parse(is);
        assertTrue(rows.isEmpty());
    }
}
