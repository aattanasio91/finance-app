package com.finance.app.category;

import com.finance.app.common.exception.BadRequestException;
import com.finance.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll(CategoryType type) {
        if (type != null) {
            return categoryRepository.findByType(type).stream()
                    .map(CategoryResponse::from)
                    .toList();
        }
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameAndType(request.name(), request.type())) {
            throw new BadRequestException("Category already exists with name: " + request.name());
        }

        Category category = new Category(request.name(), request.type(), false);
        category = categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.isSystem()) {
            throw new BadRequestException("System categories cannot be modified");
        }

        if (request.name() != null) {
            category.setName(request.name());
        }

        category = categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.isSystem()) {
            throw new BadRequestException("System categories cannot be deleted");
        }

        categoryRepository.delete(category);
    }
}
