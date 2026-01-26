package backend.biddingwars.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Auction Item Request Data Transfer Object
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
public record AuctionItemRequestDTO(
        @NotBlank String title,
        @Size(max = 2000) String description,

        @NotNull
        @Positive
        BigDecimal startingPrice,

        @NotNull
        @FutureOrPresent
        LocalDateTime startTime,

        @NotNull
        @Future
        LocalDateTime endTime,

        List<Long> categoryIds,

        Double latitude,
        Double longitude

) {}