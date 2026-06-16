package com.finance.app.importation.normalizer;

import com.finance.app.merchant.Merchant;
import com.finance.app.merchant.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MerchantNormalizer {

    private final MerchantRepository merchantRepository;

    public Optional<Merchant> normalize(String description) {
        if (description == null || description.isBlank()) return Optional.empty();

        String normalized = normalizeName(description);

        Optional<Merchant> existing = merchantRepository.findByNormalizedName(normalized);
        if (existing.isPresent()) return existing;

        Merchant merchant = new Merchant();
        merchant.setName(description.trim());
        merchant.setNormalizedName(normalized);
        merchant.setVerified(false);
        merchant = merchantRepository.save(merchant);

        return Optional.of(merchant);
    }

    public static String normalizeName(String name) {
        if (name == null) return "";
        String normalized = name.toUpperCase();
        normalized = normalized.replaceAll("[^A-Z0-9\\s]", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        normalized = removeSuffixes(normalized);
        normalized = removeLocations(normalized);

        if (normalized.isEmpty()) return name.toUpperCase().replaceAll("[^A-Z0-9\\s]", "").replaceAll("\\s+", " ").trim();

        return normalized;
    }

    private static String removeSuffixes(String name) {
        String[] words = name.split("\\s+");
        if (words.length <= 1) return name;

        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (isKnownSuffix(word)) continue;
            if (result.length() > 0) result.append(" ");
            result.append(word);
        }
        return result.toString().trim();
    }

    private static boolean isKnownSuffix(String word) {
        return switch (word) {
            case "SA", "S.A", "S.A.", "SRL", "S.R.L.", "LTDA", "E.I.", "E.I.R.L.",
                 "SAU", "S.A.U.", "SL", "S.L.", "S.L.U." -> true;
            default -> false;
        };
    }

    private static String removeLocations(String name) {
        String[] words = name.split("\\s+");
        if (words.length <= 1) return name;

        int lastIndex = words.length - 1;

        String last = words[lastIndex];
        if (last.length() == 1 || ALL_CITIES.contains(last) || ALL_PROVINCES.contains(last) || last.matches("\\d{4}")) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < lastIndex; i++) {
                if (result.length() > 0) result.append(" ");
                result.append(words[i]);
            }
            return result.toString().trim();
        }

        if (words.length >= 3) {
            String secondLast = words[lastIndex - 1];
            if (last.matches("\\d{4}") || ALL_CITIES.contains(secondLast + " " + last)) {
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < lastIndex - 1; i++) {
                    if (result.length() > 0) result.append(" ");
                    result.append(words[i]);
                }
                return result.toString().trim();
            }
        }

        return name;
    }

    private static final java.util.Set<String> ALL_CITIES = java.util.Set.of(
            "CABA", "BSAS", "BUENOS", "AIRES", "CORDOBA", "ROSARIO", "MENDOZA",
            "LA", "PLATA", "MAR", "DEL", "SAN", "MIGUEL", "TUCUMAN",
            "SALTA", "SANTA", "FE", "POSADAS", "NEUQUEN", "BARILOCHE"
    );

    private static final java.util.Set<String> ALL_PROVINCES = java.util.Set.of(
            "PBA", "PCIA", "PCIA.", "PROVINCIA"
    );
}
