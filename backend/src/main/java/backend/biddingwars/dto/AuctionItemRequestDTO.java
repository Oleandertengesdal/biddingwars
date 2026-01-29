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
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotNull(message = "Starting price is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Starting price must be positive")
        BigDecimal startingPrice,

        @NotNull(message = "Start time is required")
        @FutureOrPresent(message = "Start time must be now or in the future")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        LocalDateTime endTime,

        @NotNull(message = "At least one category is required")
        @Size(min = 1, message = "At least one category must be selected")
        List<Long> categoryIds,

        Double latitude,
        Double longitude
) {}