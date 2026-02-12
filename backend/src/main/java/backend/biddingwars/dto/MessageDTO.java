package backend.biddingwars.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Message Data Transfer Object
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
@Schema(description = "Message information")
public record MessageDTO(
        @Schema(description = "Message ID", example = "1")
        Long id,
        
        @Schema(description = "Sender user ID", example = "1")
        Long senderId,
        
        @Schema(description = "Sender username", example = "johndoe")
        String senderUsername,
        
        @Schema(description = "Receiver user ID", example = "2")
        Long receiverId,
        
        @Schema(description = "Receiver username", example = "janedoe")
        String receiverUsername,
        
        @Schema(description = "Message content")
        String content,
        
        @Schema(description = "Message timestamp")
        LocalDateTime timestamp,
        
        @Schema(description = "Related auction item ID", example = "10")
        Long itemId,
        
        @Schema(description = "Whether the message has been read")
        boolean read
) {}
