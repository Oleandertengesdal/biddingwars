package backend.biddingwars.mapper;

import org.springframework.stereotype.Component;

import backend.biddingwars.dto.UserDTO;
import backend.biddingwars.model.User;

/**
 * Mapper class for converting between User entities and UserDTOs.
 *
 * @author oleander tengesdal
 * @version 1.0
 * @since 29-01-2026
 */
@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getCreatedAt()
        );
    }
}
