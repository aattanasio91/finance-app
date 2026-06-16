package com.finance.app.importation.santander;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SantanderCardExcelParserTest {

    private final SantanderCardExcelParser parser = new SantanderCardExcelParser();

    private InputStream createWorkbook(RowWriter writer) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Fecha");
            header.createCell(1).setCellValue("Descripcion");
            header.createCell(2).setCellValue("Cuotas");
            header.createCell(3).setCellValue("Voucher");
            header.createCell(4).setCellValue("Pesos");
            header.createCell(5).setCellValue("USD");
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
            r1.createCell(1).setCellValue("NETFLIX");
            r1.createCell(2).setCellValue("");
            r1.createCell(3).setCellValue("12345678");
            r1.createCell(4).setCellValue("1.500,00");
            r1.createCell(5).setCellValue("");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertEquals(1, rows.size());
        SantanderParsedRow row = rows.get(0);
        assertEquals(LocalDate.of(2026, 5, 15), row.date());
        assertEquals("NETFLIX", row.description());
        assertEquals(0, new BigDecimal("1500.00").compareTo(row.amount()));
        assertEquals("ARS", row.currency());
        assertEquals("12345678", row.voucherNumber());
    }

    @Test
    void selectsUsdWhenPresent() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("15/05/2026");
            r1.createCell(1).setCellValue("AMAZON");
            r1.createCell(2).setCellValue("");
            r1.createCell(3).setCellValue("");
            r1.createCell(4).setCellValue("1.000,00");
            r1.createCell(5).setCellValue("10,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertEquals(1, rows.size());
        assertEquals("USD", rows.get(0).currency());
        assertEquals(0, new BigDecimal("10.00").compareTo(rows.get(0).amount()));
    }

    @Test
    void parsesInstallments() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("10/05/2026");
            r1.createCell(1).setCellValue("MERCADO LIBRE");
            r1.createCell(2).setCellValue("3 de 12");
            r1.createCell(3).setCellValue("87654321");
            r1.createCell(4).setCellValue("2.500,00");
            r1.createCell(5).setCellValue("");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertEquals(1, rows.size());
        assertEquals(Integer.valueOf(3), rows.get(0).currentInstallment());
        assertEquals(Integer.valueOf(12), rows.get(0).totalInstallments());
    }

    @Test
    void skipsSubtotalRow() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("15/05/2026");
            r1.createCell(1).setCellValue("NETFLIX");
            r1.createCell(2).setCellValue("");
            r1.createCell(3).setCellValue("");
            r1.createCell(4).setCellValue("1.500,00");
            r1.createCell(5).setCellValue("");
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("");
            r2.createCell(1).setCellValue("Subtotal");
            r2.createCell(4).setCellValue("1.500,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertEquals(1, rows.size());
    }

    @Test
    void skipsRowWithNullDate() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("");
            r1.createCell(1).setCellValue("SIN FECHA");
            r1.createCell(4).setCellValue("500,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertTrue(rows.isEmpty());
    }

    @Test
    void skipsRowWithNullAmount() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("15/05/2026");
            r1.createCell(1).setCellValue("SIN MONTO");
            r1.createCell(4).setCellValue("");
            r1.createCell(5).setCellValue("");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertTrue(rows.isEmpty());
    }

    @Test
    void handlesNullDescription() throws Exception {
        InputStream is = createWorkbook(sheet -> {
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("15/05/2026");
            r1.createCell(4).setCellValue("500,00");
        });
        List<SantanderParsedRow> rows = parser.parse(is);
        assertTrue(rows.isEmpty());
    }

    @Test
    void emptySheetReturnsNoRows() throws Exception {
        InputStream is = createWorkbook(sheet -> {});
        List<SantanderParsedRow> rows = parser.parse(is);
        assertTrue(rows.isEmpty());
    }
}
