package com.finance.app.importation.santander;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SantanderCardPdfParser {

    private static final Pattern DATE_HEADER = Pattern.compile(
            "(\\d{2})\\s+(Enero|Febrero|Marzo|Abril|Mayo|Junio|Julio|Agosto|Septiembre|Octubre|Noviembre|Diciembre|Noviem\\.?)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern VOUCHER_LINE = Pattern.compile(
            "^(\\d{1,2})\\s+(\\d{4,8})(?:\\s+([K\\*F])\\s*)?(.*)"
    );
    private static final Pattern FEE_LINE = Pattern.compile(
            "^(\\d{1,2})\\s+(\\D.*)"
    );
    private static final Pattern INSTALLMENT = Pattern.compile(
            "C\\.(\\d+)/(\\d+)", Pattern.CASE_INSENSITIVE
    );
    private static final Pattern AMOUNT = Pattern.compile(
            "([\\d]{1,3}(?:\\.[\\d]{3})*,\\d{2})"
    );
    private static final Pattern USD_MARKER = Pattern.compile(
            "\\bUSD\\b", Pattern.CASE_INSENSITIVE
    );

    public List<SantanderParsedRow> parse(InputStream inputStream) {
        List<SantanderParsedRow> rows = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            String[] lines = text.split("\\n");
            boolean inSection = false;
            boolean skipHeader = false;
            Integer currentYear = null;
            Month currentMonth = null;
            List<String> sectionLines = new ArrayList<>();

            for (String line : lines) {
                String t = line.trim();
                if (t.isEmpty()) continue;

                if (t.contains("SALDO ANTERIOR")) {
                    inSection = true;
                    continue;
                }

                if (t.toLowerCase().contains("plan") || t.contains("cuotas de $")
                        || t.contains("Abone su resumen")) {
                    rows.addAll(parseSection(sectionLines, currentYear, currentMonth));
                    break;
                }

                if (!inSection) continue;
                if (t.startsWith("____") || t.contains("Total Consumos")) continue;
                if (t.contains("SALDO ACTUAL") || t.contains("PAGO MINIMO")) continue;
                if (t.contains("EL PRESENTE ES COPIA") || t.contains("SE EMITE AL SOLO")) continue;

                if (t.contains("RESUMEN DE CUENTA")) {
                    rows.addAll(parseSection(sectionLines, currentYear, currentMonth));
                    sectionLines.clear();
                    skipHeader = true;
                    continue;
                }

                if (skipHeader) {
                    if (t.matches("^\\d.*")) {
                        skipHeader = false;
                    } else {
                        continue;
                    }
                }

                DateHeader dh = parseDateHeader(t);
                if (dh != null) {
                    rows.addAll(parseSection(sectionLines, currentYear, currentMonth));
                    sectionLines.clear();
                    currentYear = dh.year;
                    currentMonth = dh.month;
                    String rest = t.substring(dh.matchEnd).trim();
                    if (!rest.isEmpty()) sectionLines.add(rest);
                    continue;
                }

                sectionLines.add(t);
            }

            rows.addAll(parseSection(sectionLines, currentYear, currentMonth));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Santander card PDF", e);
        }
        return rows;
    }

    private List<SantanderParsedRow> parseSection(List<String> lines, Integer year, Month month) {
        List<SantanderParsedRow> rows = new ArrayList<>();
        if (lines.isEmpty() || year == null || month == null) return rows;

        int day = 0;
        String voucher = null;
        StringBuilder descBuilder = new StringBuilder();
        BigDecimal amount = null;
        Integer currentInst = null;
        Integer totalInst = null;
        boolean isUsd = false;
        boolean hasPending = false;

        for (String line : lines) {
            Matcher vm = VOUCHER_LINE.matcher(line);
            if (vm.find()) {
                if (hasPending) {
                    rows.add(createRow(year, month, day, voucher, descBuilder, amount, currentInst, totalInst, isUsd));
                }
                day = parseInt(vm.group(1), 0);
                voucher = vm.group(2);
                String marker = vm.group(3);
                String rest = vm.group(4) != null ? vm.group(4).trim() : "";
                descBuilder = new StringBuilder(marker != null ? (marker + " " + rest) : rest);
                amount = null;
                currentInst = null;
                totalInst = null;
                isUsd = USD_MARKER.matcher(line).find();
                hasPending = true;

                Matcher imVL = INSTALLMENT.matcher(line);
                if (imVL.find()) {
                    currentInst = parseInt(imVL.group(1), null);
                    totalInst = parseInt(imVL.group(2), null);
                }

                Matcher am = AMOUNT.matcher(line);
                BigDecimal lastAmt = null;
                while (am.find()) lastAmt = parseAmount(am.group(1));
                if (lastAmt != null) amount = lastAmt;
                continue;
            }

            Matcher fm = FEE_LINE.matcher(line);
            if (fm.find()) {
                if (hasPending) {
                    rows.add(createRow(year, month, day, voucher, descBuilder, amount, currentInst, totalInst, isUsd));
                }
                day = parseInt(fm.group(1), 0);
                voucher = null;
                String rest = fm.group(2).trim();
                descBuilder = new StringBuilder(rest);
                amount = null;
                currentInst = null;
                totalInst = null;
                isUsd = USD_MARKER.matcher(line).find();
                hasPending = true;

                Matcher am = AMOUNT.matcher(line);
                BigDecimal lastAmt = null;
                while (am.find()) lastAmt = parseAmount(am.group(1));
                if (lastAmt != null) amount = lastAmt;
                continue;
            }

            if (!hasPending) continue;

            Matcher im = INSTALLMENT.matcher(line);
            if (im.find()) {
                currentInst = parseInt(im.group(1), null);
                totalInst = parseInt(im.group(2), null);
                continue;
            }

            if (USD_MARKER.matcher(line).find()) {
                isUsd = true;
                continue;
            }

            Matcher am2 = AMOUNT.matcher(line);
            if (am2.find()) {
                BigDecimal lastAmt = null;
                while (am2.find()) lastAmt = parseAmount(am2.group(1));
                if (lastAmt != null) amount = lastAmt; else amount = parseAmount(am2.group(1));
                continue;
            }

            if (descBuilder.length() > 0) descBuilder.append(" ");
            descBuilder.append(line);
        }

        if (hasPending) {
            rows.add(createRow(year, month, day, voucher, descBuilder, amount, currentInst, totalInst, isUsd));
        }

        return rows;
    }

    private SantanderParsedRow createRow(int year, Month month, int day, String voucher,
                                          StringBuilder desc, BigDecimal amount,
                                          Integer currentInst, Integer totalInst, boolean isUsd) {
        if (day == 0) return null;
        String description = desc.toString().trim();
        if (description.isEmpty()) return null;

        int maxDay = month.length(Year.isLeap(year));
        LocalDate date = LocalDate.of(year, month, Math.min(day, maxDay));

        String descClean = description
                .replaceAll("C\\.\\d+/\\d+", "")
                .replaceAll("\\s+USD\\s*", " ")
                .replaceAll("\\s+U\\$S\\s*", " ")
                .replaceAll("[\\d]{1,3}(?:\\.[\\d]{3})*,\\d{2}", "")
                .replaceAll("\\$", "")
                .replaceAll("\\s{2,}", " ")
                .trim();

        return new SantanderParsedRow(date, descClean, amount, isUsd ? "USD" : "ARS",
                voucher, currentInst, totalInst, null);
    }

    private BigDecimal parseAmount(String amtStr) {
        if (amtStr == null || amtStr.isBlank()) return null;
        try {
            String cleaned = amtStr
                    .replace("U$S", "")
                    .replace("$", "")
                    .replace(".", "")
                    .replace(",", ".")
                    .trim();
            if (cleaned.isEmpty()) return null;
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String s, Integer def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def != null ? def : 0; }
    }

    private DateHeader parseDateHeader(String text) {
        Matcher m = DATE_HEADER.matcher(text);
        if (!m.find()) return null;
        int yy = parseInt(m.group(1), 0);
        if (yy == 0) return null;
        int year = 2000 + yy;
        String monthStr = m.group(2).toLowerCase();
        Month month = switch (monthStr) {
            case "enero" -> Month.JANUARY;
            case "febrero" -> Month.FEBRUARY;
            case "marzo" -> Month.MARCH;
            case "abril" -> Month.APRIL;
            case "mayo" -> Month.MAY;
            case "junio" -> Month.JUNE;
            case "julio" -> Month.JULY;
            case "agosto" -> Month.AUGUST;
            case "septiembre" -> Month.SEPTEMBER;
            case "octubre" -> Month.OCTOBER;
            case "noviembre", "noviem." -> Month.NOVEMBER;
            case "diciembre" -> Month.DECEMBER;
            default -> null;
        };
        if (month == null) return null;
        return new DateHeader(year, month, m.end());
    }

    private record DateHeader(int year, Month month, int matchEnd) {}
}
