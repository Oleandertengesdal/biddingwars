package backend.biddingwars.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.biddingwars.model.Bid;

/**
 * Repository interface for Bid entities.
 * Provides CRUD operations and custom queries for bids.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    /**
     * Find all bids for a specific auction item, ordered by amount descending.
     *
     * @param itemId the auction item ID
     * @return list of bids sorted by amount (highest first)
     */
    @Query("SELECT b FROM Bid b WHERE b.auctionItem.id = :itemId ORDER BY b.amount DESC")
    List<Bid> findByAuctionItemIdOrderByAmountDesc(@Param("itemId") Long itemId);

    /**
     * Find all bids for a specific auction item with pagination.
     *
     * @param itemId the auction item ID
     * @param pageable pagination parameters
     * @return page of bids
     */
    Page<Bid> findByAuctionItemId(Long itemId, Pageable pageable);

    /**
     * Find the highest bid for an auction item.
     *
     * @param itemId the auction item ID
     * @return Optional containing the highest bid if exists
     */
    @Query("SELECT b FROM Bid b WHERE b.auctionItem.id = :itemId ORDER BY b.amount DESC LIMIT 1")
    Optional<Bid> findHighestBidForItem(@Param("itemId") Long itemId);

    /**
     * Get the highest bid amount for an auction item.
     *
     * @param itemId the auction item ID
     * @return the highest bid amount or null if no bids
     */
    @Query("SELECT MAX(b.amount) FROM Bid b WHERE b.auctionItem.id = :itemId")
    BigDecimal findHighestBidAmount(@Param("itemId") Long itemId);

    /**
     * Find all bids placed by a specific user.
     *
     * @param bidderId the bidder's user ID
     * @return list of bids
     */
    List<Bid> findByBidderId(Long bidderId);

    /**
     * Find all bids placed by a specific user with pagination.
     *
     * @param bidderId the bidder's user ID
     * @param pageable pagination parameters
     * @return page of bids
     */
    Page<Bid> findByBidderId(Long bidderId, Pageable pageable);

    /**
     * Count the number of bids for an auction item.
     *
     * @param itemId the auction item ID
     * @return the bid count
     */
    long countByAuctionItemId(Long itemId);

    /**
     * Check if a user has already placed a bid on an item.
     *
     * @param bidderId the bidder's user ID
     * @param itemId the auction item ID
     * @return true if user has bid on this item
     */
    boolean existsByBidderIdAndAuctionItemId(Long bidderId, Long itemId);

    /**
     * Count total bids placed by a specific user.
     *
     * @param bidderId the bidder's user ID
     * @return total number of bids
     */
    long countByBidderId(Long bidderId);
}
