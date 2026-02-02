package backend.biddingwars.dto;

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
public record BidRequestDTO(
        @NotNull(message = "Auction item ID is required")
        Long itemId,

        @NotNull(message = "Bid amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Bid amount must be positive")
        BigDecimal amount
) {}
