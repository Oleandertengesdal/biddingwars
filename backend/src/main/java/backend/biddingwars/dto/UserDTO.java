package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for User entity.
 * Used for returning user information to clients.
 *
 * @author Oleander Tengesdal
 * @version 1.2
 * @since 26-01-2026
 */
@Schema(description = "User information")
public record UserDTO(
        @Schema(description = "User ID", example = "1")
        Long id,
        
        @Schema(description = "Username", example = "johndoe")
        String username,
        
        @Schema(description = "Email address", example = "john@example.com")
        String email,
        
        @Schema(description = "First name", example = "John")
        String firstName,
        
        @Schema(description = "Last name", example = "Doe")
        String lastName,
        
        @Schema(description = "User role", example = "USER")
        String role,
        
        @Schema(description = "Whether account is enabled")
        boolean enabled,
        
        @Schema(description = "Whether user has a verified payment method")
        boolean hasVerifiedPaymentMethod,
        
        @Schema(description = "Default payment method display name")
        String defaultPaymentMethod,
        
        @Schema(description = "Account creation timestamp")
        LocalDateTime createdAt
) {}