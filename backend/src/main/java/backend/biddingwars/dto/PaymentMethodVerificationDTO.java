package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for verifying a payment method.
 * Used in the mock verification flow where users enter a code
 * sent via "SMS" or displayed in Vipps-style push notification.
 */
@Schema(description = "Payment method verification request")
public record PaymentMethodVerificationDTO(
        @Schema(description = "Payment method ID to verify", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Payment method ID is required")
        Long paymentMethodId,
        
        @Schema(description = "6-digit verification code", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be 6 digits")
        String verificationCode
) {}
