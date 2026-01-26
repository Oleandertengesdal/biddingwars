package backend.biddingwars.dto;

import backend.biddingwars.model.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Auction Item Detail Data Transfer Object
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
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
        Status status,
        int bidCount,
        Double latitude,
        Double longitude
) {}
