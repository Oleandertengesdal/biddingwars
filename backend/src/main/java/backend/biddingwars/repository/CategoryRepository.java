package backend.biddingwars.repository;

import backend.biddingwars.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Category entities.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 29-01-2026
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find a category by its name.
     */
    Optional<Category> findByName(String name);

    /**
     * Check if a category with the given name exists.
     */
    boolean existsByName(String name);
}
