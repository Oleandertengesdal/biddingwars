package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Category Data Transfer Object.
 * Used for returning category information to clients.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 26-01-2026
 */
@Schema(description = "Category information")
public record CategoryDTO(
        @Schema(description = "Category ID", example = "1")
        Long id,
        
        @Schema(description = "Category name", example = "Electronics")
        String name
){}
