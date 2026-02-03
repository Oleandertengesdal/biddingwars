package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a duplicate resource already exists.
 * Examples: username/email already registered, duplicate entries.
 * Returns HTTP 409 Conflict.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public class DuplicateResourceException extends AppException {

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String resourceType, String fieldName, String value) {
        super(
                String.format("%s with %s '%s' already exists", resourceType, fieldName, value),
                HttpStatus.CONFLICT,
                "DUPLICATE_RESOURCE"
        );
    }
}
