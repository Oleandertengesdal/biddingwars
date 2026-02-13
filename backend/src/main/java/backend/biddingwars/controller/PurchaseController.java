package backend.biddingwars.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.biddingwars.dto.PaymentSubmissionDTO;
import backend.biddingwars.dto.PurchaseDTO;
import backend.biddingwars.model.User;
import backend.biddingwars.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for purchase endpoints.
 * Handles payment submissions and purchase history.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
@RestController
@RequestMapping("/purchases")
@Tag(name = "Purchases", description = "Purchase and payment management endpoints")
public class PurchaseController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * Submit payment for a purchase.
     *
     * @param paymentSubmission the payment details
     * @param currentUser the authenticated user
     * @return the updated purchase
     */
    @PostMapping("/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit payment", description = "Submits payment for a won auction")
    public ResponseEntity<PurchaseDTO> submitPayment(
            @Valid @RequestBody PaymentSubmissionDTO paymentSubmission,
            @AuthenticationPrincipal User currentUser) {
        
        logger.info("User {} submitting payment for purchase {}", 
                currentUser.getUsername(), paymentSubmission.purchaseId());
        
        PurchaseDTO purchase = purchaseService.submitPayment(paymentSubmission, currentUser);
        return ResponseEntity.ok(purchase);
    }

    /**
     * Get a specific purchase by ID.
     *
     * @param id the purchase ID
     * @param currentUser the authenticated user
     * @return the purchase details
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get purchase", description = "Returns details of a specific purchase")
    public ResponseEntity<PurchaseDTO> getPurchaseById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        PurchaseDTO purchase = purchaseService.getPurchaseById(id, currentUser);
        return ResponseEntity.ok(purchase);
    }

    /**
     * Get all purchases for the current user (as buyer).
     *
     * @param currentUser the authenticated user
     * @return list of purchases
     */
    @GetMapping("/my-purchases")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my purchases", description = "Returns all purchases made by the current user")
    public ResponseEntity<List<PurchaseDTO>> getMyPurchases(
            @AuthenticationPrincipal User currentUser) {
        
        List<PurchaseDTO> purchases = purchaseService.getMyPurchases(currentUser.getId());
        logger.info("Fetched {} purchases for user {}", purchases.size(), currentUser.getUsername());
        return ResponseEntity.ok(purchases);
    }

    /**
     * Get all sales for the current user (as seller).
     *
     * @param currentUser the authenticated user
     * @return list of sales
     */
    @GetMapping("/my-sales")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my sales", description = "Returns all sales made by the current user")
    public ResponseEntity<List<PurchaseDTO>> getMySales(
            @AuthenticationPrincipal User currentUser) {
        
        List<PurchaseDTO> sales = purchaseService.getMySales(currentUser.getId());
        logger.info("Fetched {} sales for user {}", sales.size(), currentUser.getUsername());
        return ResponseEntity.ok(sales);
    }
}
