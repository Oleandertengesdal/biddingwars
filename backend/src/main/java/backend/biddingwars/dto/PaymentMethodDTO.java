package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Payment method information")
public record PaymentMethodDTO(
        @Schema(description = "Payment method ID", example = "1")
        Long id,
        
        @Schema(description = "Payment type", example = "CREDIT_CARD", allowableValues = {"CREDIT_CARD", "DEBIT_CARD", "VIPPS"})
        String type,
        
        @Schema(description = "Last 4 digits of card (null for Vipps)", example = "1234")
        String cardLastFour,
        
        @Schema(description = "Card type (null for Vipps)", example = "VISA")
        String cardType,
        
        @Schema(description = "Cardholder name (null for Vipps)", example = "John Doe")
        String cardholderName,
        
        @Schema(description = "Card expiry month (null for Vipps)", example = "12")
        Integer expiryMonth,
        
        @Schema(description = "Card expiry year (null for Vipps)", example = "2025")
        Integer expiryYear,
        
        @Schema(description = "Masked phone number for Vipps", example = "*** ** 123")
        String vippsPhoneNumber,
        
        @Schema(description = "Whether payment method is verified")
        boolean verified,
        
        @Schema(description = "Whether this is the default payment method")
        boolean isDefault,
        
        @Schema(description = "Whether card is expired")
        boolean expired,
        
        @Schema(description = "Friendly display name", example = "Visa ending in 1234")
        String displayName,
        
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}
