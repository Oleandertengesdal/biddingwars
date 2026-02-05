package backend.biddingwars.mapper;

import org.springframework.stereotype.Component;

import backend.biddingwars.dto.UserDTO;
import backend.biddingwars.model.User;

/**
 * Mapper class for converting between User entities and UserDTOs.
 *
 * @author oleander tengesdal
 * @version 1.2
 * @since 02-02-2026
 */
@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        // Check if user has any verified payment method
        boolean hasVerifiedPayment = user.getPaymentMethods() != null &&
                user.getPaymentMethods().stream()
                        .anyMatch(pm -> pm.isVerified() && pm.canBeUsedForPayment());
        
        // Get default payment method display name
        String defaultPayment = null;
        if (user.getPaymentMethods() != null) {
            defaultPayment = user.getPaymentMethods().stream()
                    .filter(pm -> pm.isDefault() && pm.isVerified())
                    .findFirst()
                    .map(pm -> pm.getDisplayName())
                    .orElse(null);
        }
        
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole().name() : null,
                user.isEnabled(),
                hasVerifiedPayment,
                defaultPayment,
                user.getCreatedAt()
        );
    }
}
