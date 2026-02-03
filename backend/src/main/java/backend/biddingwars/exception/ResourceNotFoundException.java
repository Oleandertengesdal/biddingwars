package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * Returns HTTP 404 Not Found.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super(
                String.format("%s not found with ID: %d", resourceType, id),
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND"
        );
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(
                String.format("%s not found: %s", resourceType, identifier),
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND"
        );
    }
}
