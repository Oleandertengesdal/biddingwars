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

    public MessageDTO ToDTO(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getReceiver().getId(),
                message.getReceiver().getUsername(),
                message.getContent(),
                message.getTimestamp(),
                message.getItem().getId()
        );
    }

    public Message ToEntity(MessageDTO messageDTO) {
        Message message = new Message();
        message.setId(messageDTO.id());
        // Note: Setting sender, receiver, and item would typically require fetching these entities from the database
        // TODO: Implement fetching sender, receiver, and item entities based on their IDs
        message.setContent(messageDTO.content());
        message.setTimestamp(messageDTO.timestamp());
        return message;
    }
}
