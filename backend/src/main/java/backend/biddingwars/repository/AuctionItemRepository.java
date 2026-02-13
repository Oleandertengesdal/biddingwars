package backend.biddingwars.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.biddingwars.model.AuctionItem;
import backend.biddingwars.model.Status;
import backend.biddingwars.model.User;

/**
 * Repository interface for AuctionItem entities.
 * Provides CRUD operations and custom queries for auction items.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 29-01-2026
 */
@Repository
public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {

    /**
     * Find all auction items by status.
     */
    List<AuctionItem> findByStatus(Status status);

    /**
     * Find all auction items by status with pagination.
     */
    Page<AuctionItem> findByStatus(Status status, Pageable pageable);

    /**
     * Find all active auctions (status = ACTIVE and end time is in the future).
     */
    @Query("SELECT a FROM AuctionItem a WHERE a.status = 'ACTIVE' AND a.auctionEndTime > :now")
    List<AuctionItem> findActiveAuctions(@Param("now") LocalDateTime now);

    /**
     * Find all active auctions with pagination.
     */
    @Query("SELECT a FROM AuctionItem a WHERE a.status = 'ACTIVE' AND a.auctionEndTime > :now")
    Page<AuctionItem> findActiveAuctions(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Find all auction items owned by a specific user.
     */
    List<AuctionItem> findByOwner(User owner);

    /**
     * Find all auction items owned by a specific user ID.
     */
    List<AuctionItem> findByOwnerId(Long ownerId);

    /**
     * Find auction items by category ID.
     */
    @Query("SELECT a FROM AuctionItem a JOIN a.categories c WHERE c.id = :categoryId AND a.status = 'ACTIVE'")
    List<AuctionItem> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Search auction items by title (case-insensitive).
     */
    @Query("SELECT a FROM AuctionItem a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND a.status = 'ACTIVE'")
    List<AuctionItem> searchByTitle(@Param("searchTerm") String searchTerm);

    /**
     * Search auction items by title or description (case-insensitive).
     */
    @Query("SELECT a FROM AuctionItem a WHERE " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND a.status = 'ACTIVE'")
    Page<AuctionItem> searchByTitleOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find auctions that have ended but are still marked as ACTIVE (for scheduled cleanup).
     */
    @Query("SELECT a FROM AuctionItem a WHERE a.status = 'ACTIVE' AND a.auctionEndTime <= :now")
    List<AuctionItem> findExpiredActiveAuctions(@Param("now") LocalDateTime now);

    /**
     * Count active auctions for a specific user.
     */
    long countByOwnerIdAndStatus(Long ownerId, Status status);

    /**
     * Count total auctions for a specific user (all statuses).
     */
    long countByOwnerId(Long ownerId);
}
