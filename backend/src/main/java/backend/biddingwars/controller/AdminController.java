package backend.biddingwars.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.biddingwars.dto.BanRequestDTO;
import backend.biddingwars.dto.PurchaseDTO;
import backend.biddingwars.dto.UserAdminDTO;
import backend.biddingwars.dto.UserDTO;
import backend.biddingwars.model.User;
import backend.biddingwars.repository.AuctionItemRepository;
import backend.biddingwars.repository.BidRepository;
import backend.biddingwars.repository.PurchaseRepository;
import backend.biddingwars.service.PurchaseService;
import backend.biddingwars.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for admin endpoints.
 * Handles user management, ban operations, and administrative tasks.
 * All endpoints require ADMIN role.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative endpoints (ADMIN role required)")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final PurchaseService purchaseService;
    private final AuctionItemRepository auctionItemRepository;
    private final BidRepository bidRepository;
    private final PurchaseRepository purchaseRepository;

    public AdminController(UserService userService,
                           PurchaseService purchaseService,
                           AuctionItemRepository auctionItemRepository,
                           BidRepository bidRepository,
                           PurchaseRepository purchaseRepository) {
        this.userService = userService;
        this.purchaseService = purchaseService;
        this.auctionItemRepository = auctionItemRepository;
        this.bidRepository = bidRepository;
        this.purchaseRepository = purchaseRepository;
    }

    // ==================== USER MANAGEMENT ====================

    /**
     * Get all users with admin details.
     *
     * @param onlyBanned filter to show only banned users
     * @return list of users with admin information
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Returns all users with administrative information")
    public ResponseEntity<List<UserAdminDTO>> getAllUsers(
            @RequestParam(required = false, defaultValue = "false") boolean onlyBanned) {
        
        logger.info("Admin fetching all users (onlyBanned: {})", onlyBanned);
        
        List<UserDTO> users = onlyBanned 
                ? userService.getBannedUsers() 
                : userService.getAllUsers();
        
        List<UserAdminDTO> adminDTOs = users.stream()
                .map(this::enrichWithAdminInfo)
                .toList();
        
        return ResponseEntity.ok(adminDTOs);
    }

    /**
     * Get user details with admin information.
     *
     * @param id the user ID
     * @return user with admin details
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "Get user details", description = "Returns detailed user information including ban history")
    public ResponseEntity<UserAdminDTO> getUserById(@PathVariable Long id) {
        logger.info("Admin fetching user {}", id);
        
        UserDTO user = userService.getUserById(id);
        UserAdminDTO adminDTO = enrichWithAdminInfo(user);
        
        return ResponseEntity.ok(adminDTO);
    }

    /**
     * Ban a user.
     *
     * @param id the user ID to ban
     * @param banRequest the ban details
     * @return the updated user
     */
    @PostMapping("/users/{id}/ban")
    @Operation(summary = "Ban user", description = "Bans a user for a specified duration or permanently")
    public ResponseEntity<UserDTO> banUser(
            @PathVariable Long id,
            @Valid @RequestBody BanRequestDTO banRequest) {
        
        logger.warn("Admin banning user {}: {} (permanent: {}, days: {})",
                id, banRequest.reason(), banRequest.permanent(), banRequest.durationDays());
        
        UserDTO updatedUser = userService.banUser(
                id, 
                banRequest.reason(), 
                banRequest.durationDays(), 
                banRequest.permanent()
        );
        
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Unban a user.
     *
     * @param id the user ID to unban
     * @return the updated user
     */
    @DeleteMapping("/users/{id}/ban")
    @Operation(summary = "Unban user", description = "Removes ban from a user")
    public ResponseEntity<UserDTO> unbanUser(@PathVariable Long id) {
        logger.info("Admin unbanning user {}", id);
        
        UserDTO updatedUser = userService.unbanUser(id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Get overview of all banned users.
     *
     * @return list of banned users
     */
    @GetMapping("/bans")
    @Operation(summary = "Get banned users", description = "Returns all currently banned users")
    public ResponseEntity<List<UserAdminDTO>> getBannedUsers() {
        logger.info("Admin fetching banned users overview");
        
        List<UserDTO> bannedUsers = userService.getBannedUsers();
        List<UserAdminDTO> adminDTOs = bannedUsers.stream()
                .map(this::enrichWithAdminInfo)
                .toList();
        
        return ResponseEntity.ok(adminDTOs);
    }

    /**
     * Enable or disable a user account.
     *
     * @param id the user ID
     * @param enabled whether to enable or disable
     * @return the updated user
     */
    @PostMapping("/users/{id}/enabled")
    @Operation(summary = "Enable/disable user", description = "Enables or disables a user account")
    public ResponseEntity<UserDTO> setUserEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        
        logger.info("Admin {} user {}", enabled ? "enabling" : "disabling", id);
        
        UserDTO updatedUser = userService.setUserEnabled(id, enabled);
        return ResponseEntity.ok(updatedUser);
    }

    // ==================== PURCHASE MANAGEMENT ====================

    /**
     * Get all pending payments.
     *
     * @return list of purchases awaiting payment
     */
    @GetMapping("/purchases/pending")
    @Operation(summary = "Get pending payments", description = "Returns all purchases awaiting payment")
    public ResponseEntity<List<PurchaseDTO>> getPendingPayments() {
        logger.info("Admin fetching pending payments");
        
        List<PurchaseDTO> pendingPayments = purchaseService.getPendingPayments();
        return ResponseEntity.ok(pendingPayments);
    }

    /**
     * Cancel a purchase.
     *
     * @param id the purchase ID
     * @param reason the cancellation reason
     * @return the updated purchase
     */
    @PostMapping("/purchases/{id}/cancel")
    @Operation(summary = "Cancel purchase", description = "Cancels a pending purchase")
    public ResponseEntity<PurchaseDTO> cancelPurchase(
            @PathVariable Long id,
            @RequestParam String reason) {
        
        logger.warn("Admin cancelling purchase {}: {}", id, reason);
        
        PurchaseDTO cancelledPurchase = purchaseService.cancelPurchase(id, reason);
        return ResponseEntity.ok(cancelledPurchase);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Enrich a UserDTO with admin-specific information.
     */
    private UserAdminDTO enrichWithAdminInfo(UserDTO user) {
        User fullUser = userService.findUserOrThrow(user.id());
        
        // Get statistics
        long totalAuctions = auctionItemRepository.countByOwnerId(fullUser.getId());
        long totalBids = bidRepository.countByBidderId(fullUser.getId());
        long totalPurchases = purchaseRepository.countByBuyerIdAndPaymentDefaultedTrue(fullUser.getId());
        long failedPayments = purchaseRepository.countByBuyerIdAndPaymentDefaultedTrue(fullUser.getId());
        
        // Check if currently banned
        boolean currentlyBanned = userService.isUserBanned(fullUser);
        
        return new UserAdminDTO(
                user.id(),
                user.username(),
                user.email(),
                user.firstName(),
                user.lastName(),
                user.role(),
                user.enabled(),
                fullUser.getCreatedAt(),
                fullUser.getNonPaymentCount(),
                fullUser.getBannedUntil(),
                fullUser.getBanReason(),
                fullUser.isPermanentBan(),
                currentlyBanned,
                totalAuctions,
                totalBids,
                totalPurchases,
                failedPayments
        );
    }
}
