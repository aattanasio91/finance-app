package com.finance.app.importation.santander;

import java.util.regex.Pattern;

public class SantanderAnonymizer {

    private static final Pattern CUIL_PATTERN = Pattern.compile("\\b\\d{2}-\\d{8}-\\d{1}\\b");
    private static final Pattern CBU_CVU_PATTERN = Pattern.compile("\\b\\d{22}\\b");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\b\\d{6,12}/\\d{1,2}\\b");
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("\\b\\d{16}\\b");
    private static final Pattern CARD_LAST4 = Pattern.compile("\\b(?:tarjeta?|terminada en|nro\\.?)\\s*:?\\s*\\d{4}\\b");
    private static final Pattern DNI_PATTERN = Pattern.compile("\\b\\d{7,8}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:11|15)\\d{8}\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern NAMES_PATTERN = Pattern.compile(
            "\\b(AGUSTIN|NICOLAS|ATTANASIO|DORSCH)\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "\\b(TIGRE|VILLA SANTOS TESEI|VILLA BALLESTER|B1688AJC)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public String anonymize(String text) {
        if (text == null) return null;
        String result = text;

        result = CUIL_PATTERN.matcher(result).replaceAll("XX-XXXXXXXX-X");
        result = CBU_CVU_PATTERN.matcher(result).replaceAll("XXXXXXXXXXXXXXXXXXXXXX");
        result = ACCOUNT_NUMBER_PATTERN.matcher(result).replaceAll("XXXXXX/X");
        result = CARD_NUMBER_PATTERN.matcher(result).replaceAll("XXXXXXXXXXXXXXXX");
        result = DNI_PATTERN.matcher(result).replaceAll("XXXXXXXX");
        result = PHONE_PATTERN.matcher(result).replaceAll("11XXXXXXXX");
        result = EMAIL_PATTERN.matcher(result).replaceAll("email@anonymized.com");
        result = NAMES_PATTERN.matcher(result).replaceAll("[ANONYMIZED]");
        result = ADDRESS_PATTERN.matcher(result).replaceAll("[ANONYMIZED]");

        return result;
    }
}
