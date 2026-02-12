package backend.biddingwars.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import backend.biddingwars.service.AuctionItemService;
import backend.biddingwars.service.PurchaseService;
import backend.biddingwars.service.UserService;

/**
 * Scheduled tasks for auction management.
 * Handles automatic status updates for auctions, payment deadline enforcement,
 * and ban expiry cleanup.
 *
 * @author Oleander Tengesdal
 * @version 2.0
 * @since 02-02-2026
 */
@Component
public class AuctionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AuctionScheduler.class);

    private final AuctionItemService auctionItemService;
    private final PurchaseService purchaseService;
    private final UserService userService;

    public AuctionScheduler(AuctionItemService auctionItemService,
                            PurchaseService purchaseService,
                            UserService userService) {
        this.auctionItemService = auctionItemService;
        this.purchaseService = purchaseService;
        this.userService = userService;
    }

    /**
     * Process expired auctions every minute.
     * Finds active auctions that have passed their end time and marks them as completed.
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void processExpiredAuctions() {
        logger.debug("Running scheduled task: processExpiredAuctions");
        
        try {
            auctionItemService.processExpiredAuctions();
        } catch (Exception e) {
            logger.error("Error in scheduled task processExpiredAuctions", e);
        }
    }

    /**
     * Check for overdue payments every 5 minutes.
     * Finds purchases that have passed their payment deadline and applies penalties.
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void checkPaymentDeadlines() {
        logger.debug("Running scheduled task: checkPaymentDeadlines");
        
        try {
            purchaseService.processOverduePayments();
        } catch (Exception e) {
            logger.error("Error in scheduled task checkPaymentDeadlines", e);
        }
    }

    /**
     * Clear expired bans daily at 3 AM.
     * Removes temporary bans that have passed their expiry date.
     */
    @Scheduled(cron = "0 0 3 * * *") // Daily at 3:00 AM
    public void cleanupExpiredBans() {
        logger.debug("Running scheduled task: cleanupExpiredBans");
        
        try {
            userService.clearExpiredBans();
        } catch (Exception e) {
            logger.error("Error in scheduled task cleanupExpiredBans", e);
        }
    }
}
