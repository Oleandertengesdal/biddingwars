package backend.biddingwars.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.biddingwars.model.PaymentMethod;

/**
 * Repository interface for PaymentMethod entities.
 * Provides CRUD operations and custom queries for payment methods.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /**
     * Find all payment methods for a specific user.
     *
     * @param userId the ID of the user
     * @return list of payment methods belonging to the user
     */
    List<PaymentMethod> findByUserId(Long userId);

    /**
     * Find the default payment method for a specific user.
     *
     * @param userId the ID of the user
     * @return optional containing the default payment method if it exists
     */
    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Check if a user has at least one verified payment method.
     *
     * @param userId the ID of the user
     * @param verified the verification status to check
     * @return true if user has at least one verified payment method
     */
    boolean existsByUserIdAndVerified(Long userId, boolean verified);
}
