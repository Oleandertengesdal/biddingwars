package backend.biddingwars.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.biddingwars.model.Purchase;
import backend.biddingwars.model.PurchaseStatus;

/**
 * Repository interface for Purchase entities.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 05-02-2026
 */
@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /**
     * Find all purchases by buyer ID.
     */
    List<Purchase> findByBuyerId(Long buyerId);

    /**
     * Find all purchases by seller ID.
     */
    List<Purchase> findBySellerId(Long sellerId);

    /**
     * Find purchase by auction item ID.
     */
    Optional<Purchase> findByAuctionItemId(Long auctionItemId);

    /**
     * Find all purchases with a specific status.
     */
    List<Purchase> findByStatus(PurchaseStatus status);

    /**
     * Find purchases that have passed their payment deadline and are still pending.
     */
    @Query("SELECT p FROM Purchase p WHERE p.status = :status AND p.paymentDeadline < :now AND p.paymentDefaulted = false")
    List<Purchase> findOverduePendingPayments(
            @Param("status") PurchaseStatus status,
            @Param("now") LocalDateTime now);

    /**
     * Count failed payments for a user.
     */
    long countByBuyerIdAndPaymentDefaultedTrue(Long buyerId);
}
