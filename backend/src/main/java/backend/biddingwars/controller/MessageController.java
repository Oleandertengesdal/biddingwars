package backend.biddingwars.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.biddingwars.dto.MessageDTO;
import backend.biddingwars.dto.MessageRequestDTO;
import backend.biddingwars.model.User;
import backend.biddingwars.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for managing messages between users in the bidding wars application.
 * This controller provides endpoints for sending messages, retrieving inbox and sent messages,
 * viewing conversations, and marking messages as read.
 *
 * @author Oleander Tengesdal
 * @version 2.0
 * @since 10-02-2026
 */
@RestController
@RequestMapping("/messages")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Messages", description = "User messaging endpoints")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Send a message to another user about an auction item.
     *
     * @param request the message request
     * @param currentUser the authenticated user
     * @return the created message
     */
    @PostMapping
    @Operation(summary = "Send message", description = "Send a message to another user about an auction item")
    public ResponseEntity<MessageDTO> sendMessage(
            @Valid @RequestBody MessageRequestDTO request,
            @AuthenticationPrincipal User currentUser) {
        
        logger.info("User {} sending message to user {} about item {}",
                currentUser.getUsername(), request.recipientId(), request.itemId());
        
        MessageDTO message = messageService.sendMessage(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * Get inbox (received messages) for the current user.
     *
     * @param page page number
     * @param size page size
     * @param currentUser the authenticated user
     * @return page of received messages
     */
    @GetMapping("/inbox")
    @Operation(summary = "Get inbox", description = "Get received messages for the current user")
    public ResponseEntity<Page<MessageDTO>> getInbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<MessageDTO> messages = messageService.getInbox(currentUser.getId(), pageable);
        
        logger.info("Fetched {} inbox messages for user {}", messages.getTotalElements(), currentUser.getUsername());
        return ResponseEntity.ok(messages);
    }

    /**
     * Get sent messages for the current user.
     *
     * @param page page number
     * @param size page size
     * @param currentUser the authenticated user
     * @return page of sent messages
     */
    @GetMapping("/sent")
    @Operation(summary = "Get sent messages", description = "Get sent messages for the current user")
    public ResponseEntity<Page<MessageDTO>> getSentMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<MessageDTO> messages = messageService.getSentMessages(currentUser.getId(), pageable);
        
        logger.info("Fetched {} sent messages for user {}", messages.getTotalElements(), currentUser.getUsername());
        return ResponseEntity.ok(messages);
    }

    /**
     * Get conversation with a specific user.
     *
     * @param userId the other user's ID
     * @param page page number
     * @param size page size
     * @param currentUser the authenticated user
     * @return page of messages in the conversation
     */
    @GetMapping("/conversation/{userId}")
    @Operation(summary = "Get conversation", description = "Get conversation with a specific user")
    public ResponseEntity<Page<MessageDTO>> getConversation(
            @Parameter(description = "The other user's ID") @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User currentUser) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<MessageDTO> messages = messageService.getConversation(currentUser.getId(), userId, pageable);
        
        logger.info("Fetched conversation between {} and user {}", currentUser.getUsername(), userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get messages for a specific auction item (seller only).
     *
     * @param itemId the auction item ID
     * @param currentUser the authenticated user
     * @return list of messages for the item
     */
    @GetMapping("/item/{itemId}")
    @Operation(summary = "Get messages for item", description = "Get all messages for an auction item (seller only)")
    public ResponseEntity<List<MessageDTO>> getMessagesForItem(
            @Parameter(description = "Auction item ID") @PathVariable Long itemId,
            @AuthenticationPrincipal User currentUser) {
        
        List<MessageDTO> messages = messageService.getMessagesForItem(itemId, currentUser);
        logger.info("Fetched {} messages for item {} by owner {}", messages.size(), itemId, currentUser.getUsername());
        return ResponseEntity.ok(messages);
    }

    /**
     * Get a specific message by ID.
     *
     * @param id the message ID
     * @param currentUser the authenticated user
     * @return the message
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get message", description = "Get a specific message by ID")
    public ResponseEntity<MessageDTO> getMessageById(
            @Parameter(description = "Message ID") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        MessageDTO message = messageService.getMessageById(id, currentUser);
        return ResponseEntity.ok(message);
    }

    /**
     * Mark a message as read.
     *
     * @param id the message ID
     * @param currentUser the authenticated user
     * @return the updated message
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark as read", description = "Mark a message as read")
    public ResponseEntity<MessageDTO> markAsRead(
            @Parameter(description = "Message ID") @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        MessageDTO message = messageService.markAsRead(id, currentUser);
        logger.debug("Message {} marked as read by user {}", id, currentUser.getUsername());
        return ResponseEntity.ok(message);
    }

    /**
     * Mark all messages in a conversation as read.
     *
     * @param userId the other user's ID
     * @param currentUser the authenticated user
     * @return count of messages marked as read
     */
    @PutMapping("/conversation/{userId}/read")
    @Operation(summary = "Mark conversation as read", description = "Mark all messages in a conversation as read")
    public ResponseEntity<Integer> markConversationAsRead(
            @Parameter(description = "The other user's ID") @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        
        int count = messageService.markConversationAsRead(userId, currentUser);
        logger.info("Marked {} messages as read in conversation between {} and user {}",
                count, currentUser.getUsername(), userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get count of unread messages.
     *
     * @param currentUser the authenticated user
     * @return count of unread messages
     */
    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of unread messages")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        long count = messageService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(count);
    }

    /**
     * Get unread messages.
     *
     * @param currentUser the authenticated user
     * @return list of unread messages
     */
    @GetMapping("/unread")
    @Operation(summary = "Get unread messages", description = "Get all unread messages")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages(@AuthenticationPrincipal User currentUser) {
        List<MessageDTO> messages = messageService.getUnreadMessages(currentUser.getId());
        logger.info("Fetched {} unread messages for user {}", messages.size(), currentUser.getUsername());
        return ResponseEntity.ok(messages);
    }
}
