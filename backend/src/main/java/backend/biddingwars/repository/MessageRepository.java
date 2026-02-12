package backend.biddingwars.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.biddingwars.model.Message;

/**
 * Repository interface for Message entities.
 * Provides CRUD operations and custom queries for messages.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages received by a user.
     *
     * @param receiverId the receiver's user ID
     * @return list of messages
     */
    List<Message> findByReceiverIdOrderByTimestampDesc(Long receiverId);

    /**
     * Find all messages received by a user with pagination.
     *
     * @param receiverId the receiver's user ID
     * @param pageable pagination parameters
     * @return page of messages
     */
    Page<Message> findByReceiverId(Long receiverId, Pageable pageable);

    /**
     * Find all messages sent by a user.
     *
     * @param senderId the sender's user ID
     * @return list of messages
     */
    List<Message> findBySenderIdOrderByTimestampDesc(Long senderId);

    /**
     * Find all messages sent by a user with pagination.
     *
     * @param senderId the sender's user ID
     * @param pageable pagination parameters
     * @return page of messages
     */
    Page<Message> findBySenderId(Long senderId, Pageable pageable);

    /**
     * Find conversation between two users.
     *
     * @param userId1 the first user ID
     * @param userId2 the second user ID
     * @return list of messages between the two users
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
           "ORDER BY m.timestamp DESC")
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Find conversation between two users with pagination.
     *
     * @param userId1 the first user ID
     * @param userId2 the second user ID
     * @param pageable pagination parameters
     * @return page of messages
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1)")
    Page<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);

    /**
     * Find all messages related to a specific auction item.
     *
     * @param itemId the auction item ID
     * @return list of messages
     */
    List<Message> findByItemIdOrderByTimestampDesc(Long itemId);

    /**
     * Count unread messages for a user.
     *
     * @param receiverId the receiver's user ID
     * @return count of unread messages
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.read = false")
    long countUnreadMessages(@Param("receiverId") Long receiverId);

    /**
     * Find unread messages for a user.
     *
     * @param receiverId the receiver's user ID
     * @return list of unread messages
     */
    @Query("SELECT m FROM Message m WHERE m.receiver.id = :receiverId AND m.read = false ORDER BY m.timestamp DESC")
    List<Message> findUnreadMessages(@Param("receiverId") Long receiverId);

    /**
     * Find conversation partners for a user (users they have messaged with).
     *
     * @param userId the user ID
     * @return list of user IDs that have exchanged messages with the user
     */
    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.receiver.id ELSE m.sender.id END " +
           "FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Long> findConversationPartners(@Param("userId") Long userId);
}
