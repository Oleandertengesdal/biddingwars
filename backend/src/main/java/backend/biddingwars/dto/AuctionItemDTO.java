package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Auction Item Data Transfer Object
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
@Schema(description = "Auction item summary for list views")
public record AuctionItemDTO(
        @Schema(description = "Auction ID", example = "1")
        Long id,
        
        @Schema(description = "Auction title", example = "Vintage Guitar")
        String title,
        
        @Schema(description = "Current highest bid or starting price", example = "150.00")
        BigDecimal currentPrice,
        
        @Schema(description = "Thumbnail image URL")
        String thumbnailUrl,
        
        @Schema(description = "Auction end time")
        LocalDateTime endTime,
        
        @Schema(description = "Number of bids", example = "5")
        int bidCount
) {}