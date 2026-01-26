package backend.biddingwars.mapper;

import backend.biddingwars.model.Category;

/**
 * Mapper class for Category entity.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
public class CategoryMapper {

    public Category toEntity(String categoryName) {
        Category category = new Category();
        category.setName(categoryName);
        return category;
    }

    public String toDto(Category category) {
        return category.getName();
    }
}
