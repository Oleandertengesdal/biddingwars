package backend.biddingwars.mapper;

import backend.biddingwars.dto.CategoryDTO;
import backend.biddingwars.model.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper class for Category entity.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 26-01-2026
 */
@Component
public class CategoryMapper {

    public Category toEntity(CategoryDTO categoryDto) {
        Category category = new Category();
        category.setId(categoryDto.id());
        category.setName(categoryDto.name());
        return category;
    }

    public CategoryDTO toDto(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName()
        );
    }
}
