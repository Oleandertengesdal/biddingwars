package backend.biddingwars.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for User entity.
 * Used for returning user information to clients.
 *
 * @author Oleander Tengesdal
 * @version 1.2
 * @since 26-01-2026
 */
public record UserDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean enabled,
        boolean hasVerifiedPaymentMethod,
        String defaultPaymentMethod,
        LocalDateTime createdAt
) {}