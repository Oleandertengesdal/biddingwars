package backend.biddingwars.dto;

import java.time.LocalDateTime;

/**
 * Authentication Response Data Transfer Object.
 * Returned after successful login or registration.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 26-01-2026
 */
public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        LocalDateTime expiresAt,
        UserDTO user
) {}