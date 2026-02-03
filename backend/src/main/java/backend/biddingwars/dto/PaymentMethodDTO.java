package backend.biddingwars.dto;

import java.time.LocalDateTime;

/**
 * DTO for returning payment method information to clients.
 * Supports both card and Vipps payment types.
 * Sensitive data like full card numbers or phone numbers are never included.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public record PaymentMethodDTO(
        Long id,
        String type, // CREDIT_CARD, DEBIT_CARD, VIPPS
        
        // Card fields (null if type is VIPPS)
        String cardLastFour,
        String cardType, // VISA, MASTERCARD, AMEX
        String cardholderName,
        Integer expiryMonth,
        Integer expiryYear,
        
        // Vipps fields (null if type is CARD)
        String vippsPhoneNumber, // Masked: *** ** 123
        
        // Common fields
        boolean verified,
        boolean isDefault,
        boolean expired,
        String displayName, // Friendly display name
        LocalDateTime createdAt
) {}
