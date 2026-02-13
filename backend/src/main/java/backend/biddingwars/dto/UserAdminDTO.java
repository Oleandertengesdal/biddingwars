package backend.biddingwars.dto;

import java.time.LocalDateTime;

/**
 * DTO for admin view of user with ban information.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
public record UserAdminDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean enabled,
        LocalDateTime createdAt,
        
        // Ban information
        int nonPaymentCount,
        LocalDateTime bannedUntil,
        String banReason,
        boolean permanentBan,
        boolean currentlyBanned,
        
        // Statistics
        long totalAuctions,
        long totalBids,
        long totalPurchases,
        long failedPayments
) {}
