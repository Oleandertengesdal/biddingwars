package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Auction Item Detail Data Transfer Object.
 * Contains full auction details for single item view.
 *
 * @author Oleander Tengesdal
 * @version 1.2
 * @since 02-02-2026
 */
@Schema(description = "Full auction item details")
public record AuctionItemDetailDTO(
        @Schema(description = "Auction ID", example = "1")
        Long id,
        
        @Schema(description = "Auction title", example = "Vintage Guitar")
        String title,
        
        @Schema(description = "Item description")
        String description,
        
        @Schema(description = "Starting price", example = "100.00")
        BigDecimal startingPrice,
        
        @Schema(description = "Current price", example = "150.00")
        BigDecimal currentPrice,
        
        @Schema(description = "Auction start time")
        LocalDateTime startTime,
        
        @Schema(description = "Auction end time")
        LocalDateTime endTime,
        
        @Schema(description = "Seller username", example = "seller123")
        String sellerUsername,
        
        @Schema(description = "Seller ID", example = "5")
        Long sellerId,
        
        @Schema(description = "Category names")
        List<String> categoryNames,
        
        @Schema(description = "Image URLs")
        List<String> imageUrls,
        
        @Schema(description = "Auction status", example = "ACTIVE")
        String status,
        
        @Schema(description = "Number of bids", example = "5")
        int bidCount,
        
        @Schema(description = "Latitude for pickup location")
        Double latitude,
        
        @Schema(description = "Longitude for pickup location")
        Double longitude,
        
        @Schema(description = "Whether auction is currently active")
        boolean isActive
) {}
