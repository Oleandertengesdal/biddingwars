package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a concurrent bid conflict occurs.
 * This happens when two users try to bid at the same time
 * and optimistic locking detects the conflict.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 10-02-2026
 */
public class ConcurrentBidException extends AppException {

    /**
     * Constructs a ConcurrentBidException with a message.
     *
     * @param message the error message
     */
    public ConcurrentBidException(String message) {
        super(message, HttpStatus.CONFLICT, "CONCURRENT_BID_CONFLICT");
    }

    /**
     * Constructs a ConcurrentBidException with a default message.
     */
    public ConcurrentBidException() {
        this("Another bid was placed simultaneously. Please refresh and try again.");
    }
}
