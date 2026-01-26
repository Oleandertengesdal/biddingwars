package backend.biddingwars.mapper;

import backend.biddingwars.model.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper class for Category entity.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
@Component
public class CategoryMapper {

    public Category toEntity(Category categoryDto) {
        Category category = new Category();
        category.setId(categoryDto.getId());
        category.setName(categoryDto.getName());
        return category;
    }

    public Category toDto(Category category) {
        Category categoryDto = new Category();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        return categoryDto;
    }
}
