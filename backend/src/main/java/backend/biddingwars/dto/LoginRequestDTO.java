package backend.biddingwars.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login Request Data Transfer Object
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
public record LoginRequestDTO(
        @NotBlank String username,
        @NotBlank String password
) {}
