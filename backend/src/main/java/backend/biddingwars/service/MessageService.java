package backend.biddingwars.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.biddingwars.dto.MessageDTO;
import backend.biddingwars.dto.MessageRequestDTO;
import backend.biddingwars.exception.ResourceNotFoundException;
import backend.biddingwars.exception.UnauthorizedException;
import backend.biddingwars.exception.ValidationException;
import backend.biddingwars.mapper.MessageMapper;
import backend.biddingwars.model.AuctionItem;
import backend.biddingwars.model.Message;
import backend.biddingwars.model.User;
import backend.biddingwars.repository.AuctionItemRepository;
import backend.biddingwars.repository.MessageRepository;
import backend.biddingwars.repository.UserRepository;

/**
 * Service class for message operations.
 * Handles sending messages, retrieving conversations, and marking messages as read.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
@Service
@Transactional
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final MessageMapper messageMapper;

    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          AuctionItemRepository auctionItemRepository,
                          MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.auctionItemRepository = auctionItemRepository;
        this.messageMapper = messageMapper;
    }

    /**
     * Send a message to another user.
     *
     * @param request the message request
     * @param sender the user sending the message
     * @return the created message DTO
     */
    public MessageDTO sendMessage(MessageRequestDTO request, User sender) {
        logger.info("User {} sending message to user {} about item {}",
                sender.getUsername(), request.recipientId(), request.itemId());

        // Validate: Cannot send message to yourself
        if (sender.getId().equals(request.recipientId())) {
            throw new ValidationException("You cannot send a message to yourself");
        }

        // Find recipient
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.recipientId()));

        // Find auction item
        AuctionItem item = auctionItemRepository.findById(request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Auction", request.itemId()));

        // Create message
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(recipient);
        message.setItem(item);
        message.setContent(request.content());
        message.setRead(false);

        Message saved = messageRepository.save(message);
        logger.info("Message {} sent from {} to {} about item {}",
                saved.getId(), sender.getUsername(), recipient.getUsername(), item.getId());

        return messageMapper.toDTO(saved);
    }

    /**
     * Get inbox (received messages) for a user.
     *
     * @param userId the user ID
     * @return list of received messages
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getInbox(Long userId) {
        return messageRepository.findByReceiverIdOrderByTimestampDesc(userId).stream()
                .map(messageMapper::toDTO)
                .toList();
    }

    /**
     * Get inbox with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of received messages
     */
    @Transactional(readOnly = true)
    public Page<MessageDTO> getInbox(Long userId, Pageable pageable) {
        return messageRepository.findByReceiverId(userId, pageable)
                .map(messageMapper::toDTO);
    }

    /**
     * Get sent messages for a user.
     *
     * @param userId the user ID
     * @return list of sent messages
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getSentMessages(Long userId) {
        return messageRepository.findBySenderIdOrderByTimestampDesc(userId).stream()
                .map(messageMapper::toDTO)
                .toList();
    }

    /**
     * Get sent messages with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of sent messages
     */
    @Transactional(readOnly = true)
    public Page<MessageDTO> getSentMessages(Long userId, Pageable pageable) {
        return messageRepository.findBySenderId(userId, pageable)
                .map(messageMapper::toDTO);
    }

    /**
     * Get conversation between current user and another user.
     *
     * @param currentUserId the current user's ID
     * @param otherUserId the other user's ID
     * @return list of messages in the conversation
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getConversation(Long currentUserId, Long otherUserId) {
        return messageRepository.findConversation(currentUserId, otherUserId).stream()
                .map(messageMapper::toDTO)
                .toList();
    }

    /**
     * Get conversation with pagination.
     *
     * @param currentUserId the current user's ID
     * @param otherUserId the other user's ID
     * @param pageable pagination parameters
     * @return page of messages
     */
    @Transactional(readOnly = true)
    public Page<MessageDTO> getConversation(Long currentUserId, Long otherUserId, Pageable pageable) {
        return messageRepository.findConversation(currentUserId, otherUserId, pageable)
                .map(messageMapper::toDTO);
    }

    /**
     * Get messages related to a specific auction item.
     *
     * @param itemId the auction item ID
     * @param currentUser the current user (must be owner or participant)
     * @return list of messages
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessagesForItem(Long itemId, User currentUser) {
        // Verify item exists
        AuctionItem item = auctionItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", itemId));

        // Only owner can see all messages for their item
        if (!item.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only view messages for your own auctions");
        }

        return messageRepository.findByItemIdOrderByTimestampDesc(itemId).stream()
                .map(messageMapper::toDTO)
                .toList();
    }

    /**
     * Mark a message as read.
     *
     * @param messageId the message ID
     * @param currentUser the current user (must be the receiver)
     * @return the updated message DTO
     */
    public MessageDTO markAsRead(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        // Only receiver can mark as read
        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only mark your own received messages as read");
        }

        if (!message.isRead()) {
            message.setRead(true);
            message = messageRepository.save(message);
            logger.debug("Message {} marked as read by user {}", messageId, currentUser.getUsername());
        }

        return messageMapper.toDTO(message);
    }

    /**
     * Mark all messages in a conversation as read.
     *
     * @param otherUserId the other user's ID
     * @param currentUser the current user
     * @return count of messages marked as read
     */
    public int markConversationAsRead(Long otherUserId, User currentUser) {
        List<Message> unreadMessages = messageRepository.findConversation(currentUser.getId(), otherUserId).stream()
                .filter(m -> m.getReceiver().getId().equals(currentUser.getId()) && !m.isRead())
                .toList();

        for (Message message : unreadMessages) {
            message.setRead(true);
            messageRepository.save(message);
        }

        logger.info("Marked {} messages as read in conversation between {} and user {}",
                unreadMessages.size(), currentUser.getUsername(), otherUserId);

        return unreadMessages.size();
    }

    /**
     * Get count of unread messages for a user.
     *
     * @param userId the user ID
     * @return count of unread messages
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    /**
     * Get unread messages for a user.
     *
     * @param userId the user ID
     * @return list of unread messages
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getUnreadMessages(Long userId) {
        return messageRepository.findUnreadMessages(userId).stream()
                .map(messageMapper::toDTO)
                .toList();
    }

    /**
     * Get a specific message by ID.
     *
     * @param messageId the message ID
     * @param currentUser the current user (must be sender or receiver)
     * @return the message DTO
     */
    @Transactional(readOnly = true)
    public MessageDTO getMessageById(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        // Check access
        boolean isSender = message.getSender().getId().equals(currentUser.getId());
        boolean isReceiver = message.getReceiver().getId().equals(currentUser.getId());

        if (!isSender && !isReceiver) {
            throw new UnauthorizedException("You do not have access to this message");
        }

        return messageMapper.toDTO(message);
    }
}
