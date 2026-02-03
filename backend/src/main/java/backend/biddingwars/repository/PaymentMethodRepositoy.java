package backend.biddingwars.repository;

import backend.biddingwars.model.PaymentMethod;

import java.util.List;

/**
 * Repository interface for PaymentMethod entities.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
public interface PaymentMethodRepositoy {

    /**
     * Find all payment methods associated with a specific user ID.
     *
     * @param userId the user ID
     * @return list of payment methods
     */
    List<PaymentMethod> findByUserId(Long userId);

    /**
     * Find the default payment method for a specific user ID.
     *
     * @param userId the user ID
     * @return the default payment method
     */
    PaymentMethod findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Check if a verified payment method exists for a specific user ID.
     *
     * @param userId the user ID
     * @return true if a verified payment method exists
     */
    boolean existsByUserIdAndIsVerified(Long userId);

}
