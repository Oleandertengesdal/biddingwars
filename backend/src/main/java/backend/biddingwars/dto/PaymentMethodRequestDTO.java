package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Payment method creation request")
public record PaymentMethodRequestDTO(
        @Schema(description = "Payment type", example = "CREDIT_CARD", allowableValues = {"CREDIT_CARD", "DEBIT_CARD", "VIPPS"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Payment type is required")
        @Pattern(regexp = "CREDIT_CARD|DEBIT_CARD|VIPPS", message = "Invalid payment type")
        String type,
        
        @Schema(description = "Full card number (required for card payments)", example = "4111111111111111")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number")
        String cardNumber,
        
        @Schema(description = "CVV code (required for card payments)", example = "123")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV")
        String cvv,
        
        @Schema(description = "Cardholder name (required for card payments)", example = "John Doe")
        @Size(min = 2, max = 100, message = "Cardholder name must be between 2 and 100 characters")
        String cardholderName,
        
        @Schema(description = "Card expiry month (required for card payments)", example = "12")
        @Min(value = 1, message = "Expiry month must be between 1 and 12")
        @Max(value = 12, message = "Expiry month must be between 1 and 12")
        Integer expiryMonth,
        
        @Schema(description = "Card expiry year (required for card payments)", example = "2025")
        @Min(value = 2024, message = "Expiry year must be in the future")
        Integer expiryYear,
        
        @Schema(description = "Norwegian phone number (required for Vipps)", example = "+4712345678")
        @Pattern(regexp = "^(\\+47)?[49][0-9]{7}$", message = "Invalid Norwegian phone number")
        String phoneNumber,
        
        @Schema(description = "Set as default payment method")
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
