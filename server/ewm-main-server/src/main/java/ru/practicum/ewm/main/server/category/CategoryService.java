package ru.practicum.ewm.main.server.category;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.handler.exceptions.BadRequestException;
import ru.practicum.ewm.dto.handler.exceptions.ConflictException;
import ru.practicum.ewm.dto.handler.exceptions.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> findAll(int from, int size) {
        validatePagination(from, size);
        return categoryRepository.findAllWithOffset(from, size);
    }

    @Transactional(readOnly = true)
    public Category findById(long catId) {
        return getCategoryOrThrow(catId);
    }

    @Transactional
    public Category create(Category category) {
        String name = normalizeName(category.getName());
        validateName(name);

        ensureNameNotTaken(name);

        category.setName(name);
        return saveOrConflict(category);
    }

    @Transactional
    public Category update(long catId, Category patch) {
        Category existing = getCategoryOrThrow(catId);

        String newName = normalizeName(patch.getName());
        validateName(newName);

        if (newName.equals(existing.getName())) {
            return existing;
        }

        ensureNameNotTaken(newName);

        existing.setName(newName);
        return saveOrConflict(existing);
    }

    @Transactional
    public void delete(long catId) {
        Category existing = getCategoryOrThrow(catId);
        deleteOrConflict(existing);
    }

    // Helpers

    private Category getCategoryOrThrow(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }

    private void ensureNameNotTaken(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Integrity constraint has been violated.");
        }
    }

    private Category saveOrConflict(Category category) {
        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Integrity constraint has been violated.");
        }
    }

    private void deleteOrConflict(Category category) {
        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("The category is not empty");
        }
    }

    private void validatePagination(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("from must be >= 0 and size must be > 0");
        }
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Category name must not be blank");
        }
        if (name.length() > 50) {
            throw new BadRequestException("Category name length must be <= 50");
        }
    }
}
