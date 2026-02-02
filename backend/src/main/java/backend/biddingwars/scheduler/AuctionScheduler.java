package backend.biddingwars.scheduler;

import backend.biddingwars.service.AuctionItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for auction management.
 * Handles automatic status updates for auctions.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@Component
public class AuctionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AuctionScheduler.class);

    private final AuctionItemService auctionItemService;

    public AuctionScheduler(AuctionItemService auctionItemService) {
        this.auctionItemService = auctionItemService;
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
}
