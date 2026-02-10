package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Auction creation/update request")
public record AuctionItemRequestDTO(
        @Schema(description = "Auction title", example = "Vintage Guitar", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @Schema(description = "Item description", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @Schema(description = "Starting price", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Starting price is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Starting price must be positive")
        BigDecimal startingPrice,

        @Schema(description = "Auction start time", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Start time is required")
        @FutureOrPresent(message = "Start time must be now or in the future")
        LocalDateTime startTime,

        @Schema(description = "Auction end time", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        LocalDateTime endTime,

        @Schema(description = "Category IDs", example = "[1, 2]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "At least one category is required")
        @Size(min = 1, message = "At least one category must be selected")
        List<Long> categoryIds,

        @Schema(description = "Latitude for pickup location", example = "63.4305")
        Double latitude,
        
        @Schema(description = "Longitude for pickup location", example = "10.3951")
        Double longitude
) {}