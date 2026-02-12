package backend.biddingwars.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.biddingwars.dto.PaymentSubmissionDTO;
import backend.biddingwars.dto.PurchaseDTO;
import backend.biddingwars.exception.InvalidOperationException;
import backend.biddingwars.exception.PaymentMethodNotFoundException;
import backend.biddingwars.exception.ResourceNotFoundException;
import backend.biddingwars.exception.UnauthorizedException;
import backend.biddingwars.exception.UnverifiedPaymentMethodException;
import backend.biddingwars.mapper.PurchaseMapper;
import backend.biddingwars.model.AuctionItem;
import backend.biddingwars.model.Bid;
import backend.biddingwars.model.PaymentMethod;
import backend.biddingwars.model.Purchase;
import backend.biddingwars.model.PurchaseStatus;
import backend.biddingwars.model.Status;
import backend.biddingwars.model.User;
import backend.biddingwars.repository.BidRepository;
import backend.biddingwars.repository.PaymentMethodRepository;
import backend.biddingwars.repository.PurchaseRepository;
import backend.biddingwars.repository.UserRepository;

/**
 * Service class for purchase operations.
 * Handles creating purchases when auctions end, processing payments,
 * and enforcing payment deadlines with penalties.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
@Service
@Transactional
public class PurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);
    
    // Payment deadline in hours (48 hours = 2 days)
    private static final int PAYMENT_DEADLINE_HOURS = 48;
    
    // Number of failed payments before auto-ban
    private static final int MAX_FAILED_PAYMENTS = 3;
    
    // Ban duration in days for payment failures
    private static final int BAN_DURATION_DAYS = 30;

    private final PurchaseRepository purchaseRepository;
    private final BidRepository bidRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final PurchaseMapper purchaseMapper;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           BidRepository bidRepository,
                           PaymentMethodRepository paymentMethodRepository,
                           UserRepository userRepository,
                           PurchaseMapper purchaseMapper) {
        this.purchaseRepository = purchaseRepository;
        this.bidRepository = bidRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.userRepository = userRepository;
        this.purchaseMapper = purchaseMapper;
    }

    /**
     * Create a purchase when an auction ends with bids.
     * Called by the auction scheduler when processing expired auctions.
     *
     * @param auctionItem the completed auction item
     * @return the created purchase DTO
     */
    public PurchaseDTO createPurchaseForAuction(AuctionItem auctionItem) {
        logger.info("Creating purchase for auction {}", auctionItem.getId());

        // Verify auction is in correct state
        if (auctionItem.getStatus() != Status.SOLD) {
            throw new InvalidOperationException("Cannot create purchase for auction that is not SOLD");
        }

        // Check if purchase already exists
        if (purchaseRepository.findByAuctionItemId(auctionItem.getId()).isPresent()) {
            throw new InvalidOperationException("Purchase already exists for this auction");
        }

        // Get the winning bid
        Bid winningBid = bidRepository.findHighestBidForItem(auctionItem.getId())
                .orElseThrow(() -> new InvalidOperationException("No bids found for sold auction"));

        // Create purchase
        Purchase purchase = new Purchase();
        purchase.setAuctionItem(auctionItem);
        purchase.setSeller(auctionItem.getOwner());
        purchase.setBuyer(winningBid.getBidder());
        purchase.setAmount(winningBid.getAmount());
        purchase.setStatus(PurchaseStatus.PENDING_PAYMENT);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setPaymentDeadline(LocalDateTime.now().plusHours(PAYMENT_DEADLINE_HOURS));
        purchase.setPaymentDefaulted(false);

        Purchase saved = purchaseRepository.save(purchase);
        logger.info("Purchase {} created for auction {} - buyer: {}, amount: {}, deadline: {}",
                saved.getId(), auctionItem.getId(), winningBid.getBidder().getUsername(),
                winningBid.getAmount(), saved.getPaymentDeadline());

        return purchaseMapper.toDTO(saved);
    }

    /**
     * Submit payment for a purchase.
     *
     * @param paymentSubmission the payment details
     * @param currentUser the user submitting payment
     * @return the updated purchase DTO
     */
    public PurchaseDTO submitPayment(PaymentSubmissionDTO paymentSubmission, User currentUser) {
        logger.info("User {} submitting payment for purchase {}", 
                currentUser.getUsername(), paymentSubmission.purchaseId());

        // Find purchase
        Purchase purchase = purchaseRepository.findById(paymentSubmission.purchaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", paymentSubmission.purchaseId()));

        // Verify ownership
        if (!purchase.getBuyer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not the buyer of this purchase");
        }

        // Verify status
        if (purchase.getStatus() != PurchaseStatus.PENDING_PAYMENT) {
            throw new InvalidOperationException("This purchase is not awaiting payment. Status: " + purchase.getStatus());
        }

        // Check if deadline has passed
        if (purchase.getPaymentDeadline().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("Payment deadline has passed");
        }

        // Verify payment method
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentSubmission.paymentMethodId())
                .orElseThrow(() -> new PaymentMethodNotFoundException(paymentSubmission.paymentMethodId()));

        // Verify payment method ownership
        if (!paymentMethod.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("This payment method does not belong to you");
        }

        // Verify payment method is verified
        if (!paymentMethod.isVerified()) {
            throw new UnverifiedPaymentMethodException("Payment method must be verified before use");
        }

        // Process payment (in production, this would call a payment gateway)
        // For now, we simulate successful payment
        purchase.setPaymentMethod(paymentMethod);
        purchase.setStatus(PurchaseStatus.COMPLETED);
        purchase.setCompletedDate(LocalDateTime.now());

        Purchase saved = purchaseRepository.save(purchase);
        logger.info("Payment completed for purchase {} by user {}", saved.getId(), currentUser.getUsername());

        return purchaseMapper.toDTO(saved);
    }

    /**
     * Get all purchases for the current user (as buyer).
     *
     * @param userId the user ID
     * @return list of purchases
     */
    @Transactional(readOnly = true)
    public List<PurchaseDTO> getMyPurchases(Long userId) {
        return purchaseRepository.findByBuyerId(userId).stream()
                .map(purchaseMapper::toDTO)
                .toList();
    }

    /**
     * Get all sales for the current user (as seller).
     *
     * @param userId the user ID
     * @return list of sales
     */
    @Transactional(readOnly = true)
    public List<PurchaseDTO> getMySales(Long userId) {
        return purchaseRepository.findBySellerId(userId).stream()
                .map(purchaseMapper::toDTO)
                .toList();
    }

    /**
     * Get a specific purchase by ID.
     *
     * @param purchaseId the purchase ID
     * @param currentUser the requesting user (must be buyer or seller)
     * @return the purchase DTO
     */
    @Transactional(readOnly = true)
    public PurchaseDTO getPurchaseById(Long purchaseId, User currentUser) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", purchaseId));

        // Check if user is buyer, seller, or admin
        boolean isBuyer = purchase.getBuyer().getId().equals(currentUser.getId());
        boolean isSeller = purchase.getSeller().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isBuyer && !isSeller && !isAdmin) {
            throw new UnauthorizedException("You do not have access to this purchase");
        }

        return purchaseMapper.toDTO(purchase);
    }

    /**
     * Get all pending payment purchases (admin only).
     *
     * @return list of pending purchases
     */
    @Transactional(readOnly = true)
    public List<PurchaseDTO> getPendingPayments() {
        return purchaseRepository.findByStatus(PurchaseStatus.PENDING_PAYMENT).stream()
                .map(purchaseMapper::toDTO)
                .toList();
    }

    /**
     * Process overdue payments - called by scheduler.
     * Marks overdue payments as failed and applies penalties to users.
     */
    public void processOverduePayments() {
        logger.info("Processing overdue payments...");

        List<Purchase> overduePurchases = purchaseRepository.findOverduePendingPayments(
                PurchaseStatus.PENDING_PAYMENT,
                LocalDateTime.now()
        );

        for (Purchase purchase : overduePurchases) {
            processPaymentDefault(purchase);
        }

        logger.info("Processed {} overdue payments", overduePurchases.size());
    }

    /**
     * Process a single payment default.
     * Marks purchase as failed and applies penalties.
     *
     * @param purchase the defaulted purchase
     */
    private void processPaymentDefault(Purchase purchase) {
        logger.warn("Processing payment default for purchase {} - buyer: {}",
                purchase.getId(), purchase.getBuyer().getUsername());

        // Mark purchase as failed
        purchase.setStatus(PurchaseStatus.PAYMENT_FAILED);
        purchase.setPaymentDefaulted(true);
        purchaseRepository.save(purchase);

        // Update buyer's non-payment count
        User buyer = purchase.getBuyer();
        buyer.setNonPaymentCount(buyer.getNonPaymentCount() + 1);

        logger.info("User {} now has {} non-payment marks",
                buyer.getUsername(), buyer.getNonPaymentCount());

        // Check if user should be auto-banned
        if (buyer.getNonPaymentCount() >= MAX_FAILED_PAYMENTS) {
            buyer.setBannedUntil(LocalDateTime.now().plusDays(BAN_DURATION_DAYS));
            buyer.setBanReason(String.format(
                    "Automatic ban: %d failed payments. Last failed purchase: #%d",
                    buyer.getNonPaymentCount(), purchase.getId()
            ));
            logger.warn("User {} has been automatically banned for {} days due to {} failed payments",
                    buyer.getUsername(), BAN_DURATION_DAYS, buyer.getNonPaymentCount());
        }

        userRepository.save(buyer);

        // Optionally: Notify seller that payment failed
        // Optionally: Offer to next highest bidder
    }

    /**
     * Cancel a purchase (admin only).
     *
     * @param purchaseId the purchase ID
     * @param reason the cancellation reason
     * @return the updated purchase DTO
     */
    public PurchaseDTO cancelPurchase(Long purchaseId, String reason) {
        logger.info("Admin cancelling purchase {}: {}", purchaseId, reason);

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", purchaseId));

        if (purchase.getStatus() == PurchaseStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot cancel a completed purchase. Use refund instead.");
        }

        purchase.setStatus(PurchaseStatus.CANCELLED);
        Purchase saved = purchaseRepository.save(purchase);

        return purchaseMapper.toDTO(saved);
    }
}
