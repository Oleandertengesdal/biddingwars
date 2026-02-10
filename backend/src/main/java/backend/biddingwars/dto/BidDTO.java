package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bid Data Transfer Object.
 * Used for returning bid information to clients.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 26-01-2026
 */
@Schema(description = "Bid information")
public record BidDTO(
        @Schema(description = "Bid ID", example = "1")
        Long id,
        
        @Schema(description = "Bid amount", example = "150.00")
        BigDecimal amount,
        
        @Schema(description = "Bidder user ID", example = "5")
        Long bidderId,
        
        @Schema(description = "Bidder username", example = "johndoe")
        String bidderUsername,
        
        @Schema(description = "Auction item ID", example = "1")
        Long itemId,
        
        @Schema(description = "Bid timestamp")
        LocalDateTime timestamp
) {}
