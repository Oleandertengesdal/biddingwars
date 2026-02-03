package backend.biddingwars.exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standardized error response DTO for API errors.
 * Returned in response body for all error responses.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        Map<String, String> fieldErrors,
        String path
) {}
