package backend.biddingwars.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for banning a user.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
public record BanRequestDTO(
        @NotBlank(message = "Ban reason is required")
        @Size(min = 10, max = 500, message = "Ban reason must be between 10 and 500 characters")
        String reason,
        
        /**
         * Duration of ban in days. Null or 0 for permanent ban.
         */
        Integer durationDays,
        
        /**
         * If true, this is a permanent ban regardless of durationDays.
         */
        boolean permanent
) {}
