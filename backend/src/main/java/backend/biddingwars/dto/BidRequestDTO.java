package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Bid Request Data Transfer Object for creating new bids.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@Schema(description = "Bid placement request")
public record BidRequestDTO(
        @Schema(description = "Auction item ID to bid on", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Auction item ID is required")
        Long itemId,

        @Schema(description = "Bid amount (must be higher than current price)", example = "150.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Bid amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Bid amount must be positive")
        BigDecimal amount
) {}
