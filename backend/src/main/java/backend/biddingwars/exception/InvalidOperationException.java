package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid business operation is attempted.
 * Examples: bidding on own auction, bidding after auction ended, etc.
 * Returns HTTP 409 Conflict.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public class InvalidOperationException extends AppException {

    public InvalidOperationException(String message) {
        super(message, HttpStatus.CONFLICT, "INVALID_OPERATION");
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT, "INVALID_OPERATION");
    }
}
