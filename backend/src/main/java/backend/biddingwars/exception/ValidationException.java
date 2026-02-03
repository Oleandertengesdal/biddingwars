package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input validation fails at the business logic level.
 * Different from Bean Validation (@Valid), this handles application-level validation.
 * Returns HTTP 400 Bad Request.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
}
