package backend.biddingwars.repository;

import backend.biddingwars.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entities.
 * Provides CRUD operations and custom queries for users.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username.
     *
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given username.
     *
     * @param username the username to check
     * @return true if user exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user exists with the given email.
     *
     * @param email the email to check
     * @return true if user exists
     */
    boolean existsByEmail(String email);
}
