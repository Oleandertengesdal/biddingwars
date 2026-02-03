package backend.biddingwars.mapper;

import org.springframework.stereotype.Component;

import backend.biddingwars.dto.PaymentMethodDTO;
import backend.biddingwars.model.PaymentMethod;

/**
 * Mapper class for converting between PaymentMethod entities and PaymentMethodDTOs.
 * Automatically masks sensitive data when converting to DTO.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
@Component
public class PaymentMethodMapper {

    /**
     * Converts PaymentMethod entity to DTO.
     * Automatically masks sensitive information:
     * - Card numbers shown as last 4 digits only
     * - Phone numbers shown in masked format (*** ** 123)
     * - Full card numbers and CVV never included
     *
     * @param paymentMethod the payment method entity
     * @return PaymentMethodDTO with masked sensitive data
     */
    public PaymentMethodDTO toDTO(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }

        return new PaymentMethodDTO(
                paymentMethod.getId(),
                paymentMethod.getType().name(),
                // Card fields
                paymentMethod.getCardLastFour(),
                paymentMethod.getCardType(),
                paymentMethod.getCardholderName(),
                paymentMethod.getExpiryMonth(),
                paymentMethod.getExpiryYear(),
                // Vipps field (already masked)
                paymentMethod.getVippsPhoneNumber(),
                // Common fields
                paymentMethod.isVerified(),
                paymentMethod.isDefault(),
                paymentMethod.isExpired(),
                paymentMethod.getDisplayName(),
                paymentMethod.getCreatedAt()
        );
    }

    /**
     * Masks a phone number for display.
     * Example: +4712345678 becomes *** ** 678
     *
     * @param fullPhone the full phone number
     * @return masked phone number
     */
    public String maskPhoneNumber(String fullPhone) {
        if (fullPhone == null || fullPhone.length() < 3) {
            return "***";
        }
        // Remove country code if present
        String cleaned = fullPhone.replaceAll("[^0-9]", "");
        if (cleaned.length() >= 8) {
            String lastThree = cleaned.substring(cleaned.length() - 3);
            return "*** ** " + lastThree;
        }
        return "***";
    }

    /**
     * Extracts last 4 digits from card number.
     *
     * @param cardNumber the full card number
     * @return last 4 digits
     */
    public String getLastFourDigits(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return null;
        }
        String cleaned = cardNumber.replaceAll("[^0-9]", "");
        return cleaned.substring(cleaned.length() - 4);
    }

    /**
     * Determines card type from card number.
     * Uses basic BIN (Bank Identification Number) rules.
     *
     * @param cardNumber the card number
     * @return card type (VISA, MASTERCARD, AMEX, or UNKNOWN)
     */
    public String determineCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "UNKNOWN";
        }

        String cleaned = cardNumber.replaceAll("[^0-9]", "");
        
        // Visa: starts with 4
        if (cleaned.startsWith("4")) {
            return "VISA";
        }
        // Mastercard: starts with 51-55 or 2221-2720
        if (cleaned.matches("^5[1-5].*") || cleaned.matches("^2[2-7].*")) {
            return "MASTERCARD";
        }
        // American Express: starts with 34 or 37
        if (cleaned.startsWith("34") || cleaned.startsWith("37")) {
            return "AMEX";
        }

        return "UNKNOWN";
    }
}
