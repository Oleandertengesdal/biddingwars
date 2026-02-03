package backend.biddingwars.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or adding a payment method.
 * Validates different fields based on payment type.
 * For security, full card numbers and CVV are handled but never stored.
 */
public record PaymentMethodRequestDTO(
        @NotBlank(message = "Payment type is required")
        @Pattern(regexp = "CREDIT_CARD|DEBIT_CARD|VIPPS", message = "Invalid payment type")
        String type,
        
        // Card fields - required only if type is CREDIT_CARD or DEBIT_CARD
        @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number")
        String cardNumber, // Full card number (only used for mock verification, not stored)
        
        @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV")
        String cvv, // CVV (only used for mock verification, not stored)
        
        @Size(min = 2, max = 100, message = "Cardholder name must be between 2 and 100 characters")
        String cardholderName,
        
        @Min(value = 1, message = "Expiry month must be between 1 and 12")
        @Max(value = 12, message = "Expiry month must be between 1 and 12")
        Integer expiryMonth,
        
        @Min(value = 2024, message = "Expiry year must be in the future")
        Integer expiryYear,
        
        // Vipps fields - required only if type is VIPPS
        @Pattern(regexp = "^(\\+47)?[49][0-9]{7}$", message = "Invalid Norwegian phone number")
        String phoneNumber, // Norwegian phone: +4712345678 or 12345678 (must start with 4 or 9)
        
        // Common field
        boolean setAsDefault
) {
    /**
     * Validates that required fields are present based on payment type.
     */
    public void validate() {
        if (type == null) {
            throw new IllegalArgumentException("Payment type is required");
        }
        
        if (type.equals("VIPPS")) {
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                throw new IllegalArgumentException("Phone number is required for Vipps");
            }
        } else if (type.equals("CREDIT_CARD") || type.equals("DEBIT_CARD")) {
            if (cardNumber == null || cardNumber.isEmpty()) {
                throw new IllegalArgumentException("Card number is required for card payments");
            }
            if (cvv == null || cvv.isEmpty()) {
                throw new IllegalArgumentException("CVV is required for card payments");
            }
            if (cardholderName == null || cardholderName.isEmpty()) {
                throw new IllegalArgumentException("Cardholder name is required for card payments");
            }
            if (expiryMonth == null || expiryYear == null) {
                throw new IllegalArgumentException("Expiry date is required for card payments");
            }
        }
    }
}
