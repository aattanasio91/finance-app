package com.finance.app.importation.classifier;

import com.finance.app.category.Category;
import com.finance.app.category.CategoryRepository;
import com.finance.app.category.CategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionClassifier {

    private final CategoryRepository categoryRepository;

    public Optional<Category> classify(String description) {
        if (description == null) return Optional.empty();

        String upper = description.toUpperCase();

        for (Rule rule : RULES) {
            if (upper.contains(rule.keyword)) {
                Optional<Category> category = categoryRepository.findByNameAndType(rule.categoryName, CategoryType.EXPENSE);
                if (category.isPresent()) return category;
            }
        }

        return Optional.empty();
    }

    private record Rule(String keyword, String categoryName) {}

    private static final Rule[] RULES = {
            new Rule("YPF", "Combustible"),
            new Rule("SHELL", "Combustible"),
            new Rule("AXION", "Combustible"),
            new Rule("PETROBRAS", "Combustible"),
            new Rule("GNC", "Combustible"),

            new Rule("COTO", "Supermercado"),
            new Rule("DISCO", "Supermercado"),
            new Rule("JUMBO", "Supermercado"),
            new Rule("CARREFOUR", "Supermercado"),
            new Rule("DIA", "Supermercado"),
            new Rule("CHANGOMAS", "Supermercado"),
            new Rule("VEIA", "Supermercado"),
            new Rule("DIARCO", "Supermercado"),

            new Rule("NETFLIX", "Streaming"),
            new Rule("SPOTIFY", "Streaming"),
            new Rule("DISNEY", "Streaming"),
            new Rule("HBO", "Streaming"),
            new Rule("MAX", "Streaming"),
            new Rule("PRIME VIDEO", "Streaming"),
            new Rule("AMAZON PRIME", "Streaming"),
            new Rule("STAR PLUS", "Streaming"),
            new Rule("PARAMOUNT", "Streaming"),
            new Rule("CRUNCHYROLL", "Streaming"),
            new Rule("APPLE TV", "Streaming"),

            new Rule("MC DONALDS", "Restaurantes"),
            new Rule("MCDONALDS", "Restaurantes"),
            new Rule("MCDONALD", "Restaurantes"),
            new Rule("MOSTAZA", "Restaurantes"),
            new Rule("BURGER KING", "Restaurantes"),
            new Rule("KFC", "Restaurantes"),
            new Rule("STARBUCKS", "Restaurantes"),
            new Rule("HAVANNA", "Restaurantes"),
            new Rule("PEDIDOSYA", "Restaurantes"),
            new Rule("RAPPI", "Restaurantes"),
            new Rule("MELI", "Restaurantes"),

            new Rule("EDESUR", "Servicios"),
            new Rule("EDENOR", "Servicios"),
            new Rule("AYSA", "Servicios"),
            new Rule("AGUA", "Servicios"),
            new Rule("GAS", "Servicios"),
            new Rule("METROGAS", "Servicios"),
            new Rule("OSDE", "Salud"),
            new Rule("SWISS MEDICAL", "Salud"),
            new Rule("FARMACITY", "Salud"),
            new Rule("FARMA", "Salud"),

            new Rule("PERSONAL", "Internet"),
            new Rule("CLARO", "Internet"),
            new Rule("MOVISTAR", "Internet"),
            new Rule("TELECOM", "Internet"),
            new Rule("FIBERTEL", "Internet"),
            new Rule("DIRECTV", "Streaming"),
            new Rule("FLOW", "Streaming"),
            new Rule("TELECENTRO", "Internet"),

            new Rule("UBER", "Transporte"),
            new Rule("DIDI", "Transporte"),
            new Rule("CABIFY", "Transporte"),
            new Rule("SUBE", "Transporte"),
            new Rule("ALQUILER", "Alquiler"),
            new Rule("EXPENSA", "Alquiler"),

            new Rule("MERCADO PAGO", "Servicios"),
            new Rule("MERCADOPAGO", "Servicios"),
    };
}
