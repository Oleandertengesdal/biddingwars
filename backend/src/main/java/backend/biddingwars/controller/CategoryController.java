package backend.biddingwars.controller;

import backend.biddingwars.dto.CategoryDTO;
import backend.biddingwars.mapper.CategoryMapper;
import backend.biddingwars.model.Category;
import backend.biddingwars.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for category endpoints.
 * Handles retrieval of auction categories.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Get all categories.
     *
     * @return list of all categories
     */
    @GetMapping
    @Operation(summary = "Get all categories", description = "Returns all auction categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(categoryMapper::toDto)
                .toList();
        return ResponseEntity.ok(categoryDTOs);
    }

    /**
     * Get a category by ID.
     *
     * @param id the category ID
     * @return the category
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Returns a specific category")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
        return ResponseEntity.ok(categoryMapper.toDto(category));
    }
}
