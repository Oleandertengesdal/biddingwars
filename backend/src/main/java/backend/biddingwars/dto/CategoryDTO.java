package backend.biddingwars.dto;

/**
 * Category Data Transfer Object.
 * Used for returning category information to clients.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 26-01-2026
 */
public record CategoryDTO(
        Long id,
        String name
){}
