package backend.biddingwars.mapper;

import backend.biddingwars.dto.UserDTO;
import backend.biddingwars.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between User entities and UserDTOs.
 *
 * @author oleander tengesdal
 * @version 1.0
 * @since 29-01-2026
 */
@Component
public class UserMapper {

    public UserDTO ToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    public User ToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.id());
        user.setUsername(userDTO.username());
        user.setEmail(userDTO.email());
        user.setFirstName(userDTO.firstName());
        user.setLastName(userDTO.lastName());
        user.setRole(userDTO.role());
        return user;
    }
}
