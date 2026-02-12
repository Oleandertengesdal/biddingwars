package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a banned user attempts a restricted action.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
public class UserBannedException extends AppException {

    private static final String ERROR_CODE = "USER_BANNED";

    public UserBannedException(String message) {
        super(message, HttpStatus.FORBIDDEN, ERROR_CODE);
    }

    public UserBannedException(String username, String reason) {
        super(String.format("User %s is banned: %s", username, reason), HttpStatus.FORBIDDEN, ERROR_CODE);
    }
}
