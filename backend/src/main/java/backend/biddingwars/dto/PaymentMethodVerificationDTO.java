package backend.biddingwars.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for verifying a payment method.
 * Used in the mock verification flow where users enter a code
 * sent via "SMS" or displayed in Vipps-style push notification.
 */
public record PaymentMethodVerificationDTO(
        @NotNull(message = "Payment method ID is required")
        Long paymentMethodId,
        
        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be 6 digits")
        String verificationCode
) {}
