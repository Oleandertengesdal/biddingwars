package backend.biddingwars.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

/**
 * Controller for managing messages between users in the bidding wars application.
 * This controller provides endpoints for sending messages, retrieving inbox and sent messages, viewing conversations, and marking messages as read.
 * 
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 10-02-2026
 */
@RestController
@RequestMapping("/messages")
@Validated
public class MessageController {
    @PostMapping                                    // Send message
    @GetMapping("/inbox")                          // Get received messages
    @GetMapping("/sent")                           // Get sent messages
    @GetMapping("/conversation/{userId}")          // Get conversation with user
    @PutMapping("/{id}/read")                      // Mark as read
}
