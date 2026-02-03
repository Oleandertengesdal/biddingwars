package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Base custom exception class for the application.
 * All application-specific exceptions should extend this class.
 * Provides HTTP status code and error code for consistent error handling.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public abstract class AppException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    /**
     * Constructs an AppException with message and HTTP status.
     *
     * @param message the error message
     * @param httpStatus the HTTP status to return
     * @param errorCode a unique error code for the error
     */
    public AppException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * Constructs an AppException with message, cause, and HTTP status.
     *
     * @param message the error message
     * @param cause the cause of the exception
     * @param httpStatus the HTTP status to return
     * @param errorCode a unique error code for the error
     */
    public AppException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * Get the HTTP status for this exception.
     *
     * @return the HTTP status
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Get the error code for this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
