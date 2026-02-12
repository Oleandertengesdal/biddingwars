package backend.biddingwars.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for submitting payment for a purchase.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
public record PaymentSubmissionDTO(
        @NotNull(message = "Purchase ID is required")
        Long purchaseId,
        
        @NotNull(message = "Payment method ID is required")
        Long paymentMethodId
) {}
