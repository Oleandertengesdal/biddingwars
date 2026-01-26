package backend.biddingwars.service;

import backend.biddingwars.dto.UserDTO;
import backend.biddingwars.model.User;

/**
 * Service class for user-related operations.
 */
public class UserService {
    public UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}
