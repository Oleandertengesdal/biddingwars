package backend.biddingwars.dto;

/**
 * Authentication Response Data Transfer Object
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        UserDTO user
) {}