package com.finance.app.common.config;

import com.finance.app.category.Category;
import com.finance.app.category.CategoryRepository;
import com.finance.app.category.CategoryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Categories already seeded, skipping");
            return;
        }

        log.info("Seeding default categories...");

        categoryRepository.save(new Category("Sueldo", CategoryType.INCOME, true));
        categoryRepository.save(new Category("Freelance", CategoryType.INCOME, true));
        categoryRepository.save(new Category("Inversiones", CategoryType.INCOME, true));
        categoryRepository.save(new Category("Aguinaldo", CategoryType.INCOME, true));
        categoryRepository.save(new Category("Bonos", CategoryType.INCOME, true));

        categoryRepository.save(new Category("Combustible", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Supermercado", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Streaming", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Restaurantes", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Alquiler", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Servicios", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Educacion", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Salud", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Transporte", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Indumentaria", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Entretenimiento", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Internet", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Seguros", CategoryType.EXPENSE, true));
        categoryRepository.save(new Category("Patente", CategoryType.EXPENSE, true));

        log.info("Seeded {} default categories", categoryRepository.count());
    }
}
