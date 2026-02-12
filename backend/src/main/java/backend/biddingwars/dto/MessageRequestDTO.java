package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for sending a new message.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
@Schema(description = "Message send request")
public record MessageRequestDTO(
        @Schema(description = "Recipient user ID", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Recipient ID is required")
        Long recipientId,
        
        @Schema(description = "Related auction item ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Item ID is required")
        Long itemId,
        
        @Schema(description = "Message content", example = "Is this item still available?", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Message content is required")
        @Size(min = 1, max = 2000, message = "Message must be between 1 and 2000 characters")
        String content
) {}
