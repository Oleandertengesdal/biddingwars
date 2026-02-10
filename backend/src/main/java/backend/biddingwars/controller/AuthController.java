package backend.biddingwars.controller;

import backend.biddingwars.dto.*;
import backend.biddingwars.mapper.UserMapper;
import backend.biddingwars.model.User;
import backend.biddingwars.security.JwtUtil;
import backend.biddingwars.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.validation.annotation.Validated;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * REST Controller for authentication endpoints.
 * Handles user registration and login.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtil jwtUtil,
                          UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    /**
     * Register a new user.
     *
     * @param registerRequest the registration data
     * @return the created user and JWT token
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        logger.info("Registration request for username: {}", registerRequest.username());

        UserDTO userDTO = userService.registerUser(registerRequest);

        // Generate token for the new user
        String token = jwtUtil.generateToken(userDTO.id(), userDTO.username(), userDTO.role());
        LocalDateTime expiresAt = LocalDateTime.now().plus(jwtUtil.getTokenValidityMs(), ChronoUnit.MILLIS);

        AuthResponseDTO response = new AuthResponseDTO(token, "Bearer", expiresAt, userDTO);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate a user and return a JWT token.
     *
     * @param loginRequest the login credentials
     * @return the JWT token and user info
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        logger.info("Login attempt for username: {}", loginRequest.username());

        // Authenticate using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        User user = (User) authentication.getPrincipal();
        UserDTO userDTO = userMapper.toDTO(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        LocalDateTime expiresAt = LocalDateTime.now().plus(jwtUtil.getTokenValidityMs(), ChronoUnit.MILLIS);

        logger.info("User {} logged in successfully", user.getUsername());

        AuthResponseDTO response = new AuthResponseDTO(token, "Bearer", expiresAt, userDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current authenticated user info.
     *
     * @return the current user's information
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info returned successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        logger.info("Fetching current user info for {}", user.getUsername());
        
        return ResponseEntity.ok(userMapper.toDTO(user));
    }
}
