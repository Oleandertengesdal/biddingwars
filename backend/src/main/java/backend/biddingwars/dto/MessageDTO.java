package backend.biddingwars.dto;

import java.time.LocalDateTime;

/**
 * Message Data Transfer Object
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
public record MessageDTO(
        Long id,
        Long senderId,
        String senderUsername,
        Long receiverId,
        String receiverUsername,
        String content,
        LocalDateTime timestamp,
        Long itemId
) {}
