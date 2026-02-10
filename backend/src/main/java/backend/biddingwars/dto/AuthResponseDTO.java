package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Authentication Response Data Transfer Object.
 * Returned after successful login or registration.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 26-01-2026
 */
@Schema(description = "Authentication response containing JWT token and user info")
public record AuthResponseDTO(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        
        @Schema(description = "Token type", example = "Bearer")
        String tokenType,
        
        @Schema(description = "Token expiration timestamp")
        LocalDateTime expiresAt,
        
        @Schema(description = "Authenticated user information")
        UserDTO user
) {}