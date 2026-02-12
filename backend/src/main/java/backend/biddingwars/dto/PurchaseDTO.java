package backend.biddingwars.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Purchase response.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
public record PurchaseDTO(
        Long id,
        Long auctionItemId,
        String auctionTitle,
        String thumbnailUrl,
        Long sellerId,
        String sellerUsername,
        Long buyerId,
        String buyerUsername,
        BigDecimal amount,
        String status,
        LocalDateTime purchaseDate,
        LocalDateTime paymentDeadline,
        LocalDateTime completedDate,
        boolean paymentDefaulted
) {}
