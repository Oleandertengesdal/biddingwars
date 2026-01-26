package backend.biddingwars.dto;

import backend.biddingwars.model.Role;

/**
 * Data Transfer Object for User entity.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
public record UserDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        Role role
) { }