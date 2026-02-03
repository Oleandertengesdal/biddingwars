package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user attempts an operation requiring a verified payment method
 * but doesn't have one.
 * Returns 403 FORBIDDEN status.
 * 
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public class UnverifiedPaymentMethodException extends AppException {

    public UnverifiedPaymentMethodException() {
        super("A verified payment method is required to perform this action", 
              HttpStatus.FORBIDDEN, 
              "UNVERIFIED_PAYMENT_METHOD");
    }

    public UnverifiedPaymentMethodException(String message) {
        super(message, HttpStatus.FORBIDDEN, "UNVERIFIED_PAYMENT_METHOD");
    }
}
