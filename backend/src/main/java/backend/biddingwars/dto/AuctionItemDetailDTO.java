package backend.biddingwars.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import backend.biddingwars.model.Status;

/**
 * Auction Item Detail Data Transfer Object.
 * Contains full auction details for single item view.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 02-02-2026
 */
public record AuctionItemDetailDTO(
        Long id,
        String title,
        String description,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String sellerUsername,
        Long sellerId,
        List<String> categoryNames,
        List<String> imageUrls,
        String status,
        int bidCount,
        Double latitude,
        Double longitude,
        boolean isActive
) {}
