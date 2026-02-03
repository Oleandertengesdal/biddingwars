package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when payment method verification fails.
 * Returns 400 BAD REQUEST status.
 * 
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public class PaymentVerificationException extends AppException {

    public PaymentVerificationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "PAYMENT_VERIFICATION_FAILED");
    }

    public PaymentVerificationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "PAYMENT_VERIFICATION_FAILED");
    }
}
