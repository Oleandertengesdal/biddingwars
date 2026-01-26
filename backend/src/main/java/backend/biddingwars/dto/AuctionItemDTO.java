package backend.biddingwars.dto;

import java.time.LocalDateTime;

/**
 * Auction Item Data Transfer Object
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
public record AuctionItemDTO(
        Long id,
        String title,
        Double currentPrice,
        String thumbnailUrl,
        LocalDateTime endTime,
        int bidCount
) {}