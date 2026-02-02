package backend.biddingwars.dto;

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
public record BidDTO(
        Long id,
        BigDecimal amount,
        Long bidderId,
        String bidderUsername,
        Long itemId,
        LocalDateTime timestamp
) {}
