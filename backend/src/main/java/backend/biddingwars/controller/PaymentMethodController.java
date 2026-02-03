package backend.biddingwars.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.biddingwars.dto.PaymentMethodDTO;
import backend.biddingwars.dto.PaymentMethodRequestDTO;
import backend.biddingwars.dto.PaymentMethodVerificationDTO;
import backend.biddingwars.model.User;
import backend.biddingwars.service.PaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for payment method endpoints.
 * Handles adding, verifying, and managing payment methods.
 * All endpoints require authentication.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
@RestController
@RequestMapping("/payment-methods")
@Tag(name = "Payment Methods", description = "Payment method management endpoints")
public class PaymentMethodController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodController.class);

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * Add a new payment method for the authenticated user.
     * Supports credit cards, debit cards, and Vipps.
     * Returns verification code for mock verification.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add payment method", description = "Add a new payment method (card or Vipps)")
    public ResponseEntity<PaymentMethodDTO> addPaymentMethod(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PaymentMethodRequestDTO request) {
        
        logger.info("POST /payment-methods - User {} adding payment method", user.getId());
        
        PaymentMethodDTO paymentMethod = paymentMethodService.addPaymentMethod(user.getId(), request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMethod);
    }

    /**
     * Verify a payment method using verification code.
     * Mock verification - in production would verify via micro-charge.
     */
    @PostMapping("/{id}/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify payment method", description = "Verify a payment method using the verification code")
    public ResponseEntity<PaymentMethodDTO> verifyPaymentMethod(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Payment method ID") @PathVariable Long id,
            @Valid @RequestBody PaymentMethodVerificationDTO verificationDTO) {
        
        logger.info("POST /payment-methods/{}/verify - User {} verifying payment method", id, user.getId());
        
        PaymentMethodDTO paymentMethod = paymentMethodService.verifyPaymentMethod(user.getId(), verificationDTO);
        
        return ResponseEntity.ok(paymentMethod);
    }

    /**
     * Set a payment method as the default for the user.
     * Must be verified to set as default.
     */
    @PutMapping("/{id}/default")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set default payment method", description = "Set a verified payment method as the default")
    public ResponseEntity<PaymentMethodDTO> setDefaultPaymentMethod(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Payment method ID") @PathVariable Long id) {
        
        logger.info("PUT /payment-methods/{}/default - User {} setting default", id, user.getId());
        
        PaymentMethodDTO paymentMethod = paymentMethodService.setDefaultPaymentMethod(user.getId(), id);
        
        return ResponseEntity.ok(paymentMethod);
    }

    /**
     * Get all payment methods for the authenticated user.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user payment methods", description = "Get all payment methods for the authenticated user")
    public ResponseEntity<List<PaymentMethodDTO>> getUserPaymentMethods(
            @AuthenticationPrincipal User user) {
        
        logger.info("GET /payment-methods - User {} fetching payment methods", user.getId());
        
        List<PaymentMethodDTO> paymentMethods = paymentMethodService.getUserPaymentMethods(user.getId());
        
        return ResponseEntity.ok(paymentMethods);
    }

    /**
     * Delete a payment method.
     * If it's the default, another verified method will be set as default.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete payment method", description = "Delete a payment method")
    public ResponseEntity<Void> deletePaymentMethod(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Payment method ID") @PathVariable Long id) {
        
        logger.info("DELETE /payment-methods/{} - User {} deleting payment method", id, user.getId());
        
        paymentMethodService.deletePaymentMethod(user.getId(), id);
        
        return ResponseEntity.noContent().build();
    }
}
