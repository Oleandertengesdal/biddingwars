package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user lacks permission to perform an action.
 * Examples: updating another user's auction, accessing restricted resources.
 * Returns HTTP 403 Forbidden.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "UNAUTHORIZED");
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN, "UNAUTHORIZED");
    }
}
