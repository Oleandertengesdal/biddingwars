package backend.biddingwars.exception;

/**
 * Exception thrown when a category is not found.
 * This is a specialized version of ResourceNotFoundException.
 *
 * @deprecated Use {@link ResourceNotFoundException} instead
 * @author Oleander Tengesdal
 * @version 2.0
 * @since 03-02-2026
 */
@Deprecated(since = "2.0", forRemoval = true)
public class CategoryNotFoundException extends ResourceNotFoundException {
    
    public CategoryNotFoundException(String message) {
        super(message);
    }
}