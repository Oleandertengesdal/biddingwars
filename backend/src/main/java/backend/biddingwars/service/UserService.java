package backend.biddingwars.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.biddingwars.dto.RegisterRequestDTO;
import backend.biddingwars.dto.UserDTO;
import backend.biddingwars.exception.DuplicateResourceException;
import backend.biddingwars.exception.ResourceNotFoundException;
import backend.biddingwars.exception.ValidationException;
import backend.biddingwars.mapper.UserMapper;
import backend.biddingwars.model.Role;
import backend.biddingwars.model.User;
import backend.biddingwars.repository.UserRepository;

/**
 * Service class for user-related operations.
 * Implements UserDetailsService for Spring Security integration.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, 
                       UserMapper userMapper, 
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Load user by username for Spring Security.
     *
     * @param username the username to search for
     * @return UserDetails for authentication
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }

    /**
     * Register a new user.
     *
     * @param registerRequest the registration data
     * @return the created user as DTO
     * @throws DuplicateResourceException if username or email already exists
     */
    public UserDTO registerUser(RegisterRequestDTO registerRequest) {
        logger.info("Registering new user: {}", registerRequest.username());

        // Check for existing username
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new DuplicateResourceException("User", "username", registerRequest.username());
        }

        // Check for existing email
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new DuplicateResourceException("User", "email", registerRequest.email());
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setFirstName(registerRequest.firstName());
        user.setLastName(registerRequest.lastName());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setRole(Role.USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());

        return userMapper.toDTO(savedUser);
    }

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user as DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = findUserOrThrow(id);
        return userMapper.toDTO(user);
    }

    /**
     * Get a user by username.
     *
     * @param username the username
     * @return the user as DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        return userMapper.toDTO(user);
    }

    /**
     * Get all users (admin only).
     *
     * @return list of all users as DTOs
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    /**
     * Update user profile.
     *
     * @param id the user ID
     * @param firstName updated first name
     * @param lastName updated last name
     * @param email updated email
     * @return the updated user as DTO
     */
    public UserDTO updateProfile(Long id, String firstName, String lastName, String email) {
        User user = findUserOrThrow(id);

        // Check if email is being changed and if it's already in use
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);

        User savedUser = userRepository.save(user);
        logger.info("User profile updated: {}", savedUser.getUsername());

        return userMapper.toDTO(savedUser);
    }

    /**
     * Change user password.
     *
     * @param id the user ID
     * @param currentPassword the current password for verification
     * @param newPassword the new password
     * @throws ValidationException if current password is incorrect
     */
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = findUserOrThrow(id);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password changed for user: {}", user.getUsername());
    }

    /**
     * Enable or disable a user account (admin only).
     *
     * @param id the user ID
     * @param enabled whether the account should be enabled
     * @return the updated user as DTO
     */
    public UserDTO setUserEnabled(Long id, boolean enabled) {
        User user = findUserOrThrow(id);
        user.setEnabled(enabled);
        User savedUser = userRepository.save(user);
        
        logger.info("User {} {} by admin", user.getUsername(), enabled ? "enabled" : "disabled");
        return userMapper.toDTO(savedUser);
    }

    /**
     * Delete a user account.
     *
     * @param id the user ID
     */
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
        logger.info("User deleted: {}", user.getUsername());
    }

    /**
     * Find user entity by ID or throw exception.
     */
    public User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
