package backend.biddingwars.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested payment method is not found.
 * Returns 404 NOT FOUND status.
 * 
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public class PaymentMethodNotFoundException extends AppException {

    public PaymentMethodNotFoundException(Long id) {
        super("Payment method with ID " + id + " not found", HttpStatus.NOT_FOUND, "PAYMENT_METHOD_NOT_FOUND");
    }

    public PaymentMethodNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "PAYMENT_METHOD_NOT_FOUND");
    }
}
