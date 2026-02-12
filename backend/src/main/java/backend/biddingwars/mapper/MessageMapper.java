package backend.biddingwars.mapper;

import backend.biddingwars.dto.MessageDTO;
import backend.biddingwars.model.Message;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Message entities and MessageDTOs.
 *
 * @author oleander tengesdal
 * @version 1.0
 * @since 29-01-2026
 */
@Component
public class MessageMapper {

    public MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }
        return new MessageDTO(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getReceiver().getId(),
                message.getReceiver().getUsername(),
                message.getContent(),
                message.getTimestamp(),
                message.getItem() != null ? message.getItem().getId() : null,
                message.isRead()
        );
    }
}
