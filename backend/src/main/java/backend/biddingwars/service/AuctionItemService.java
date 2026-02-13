package backend.biddingwars.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.biddingwars.dto.AuctionItemDTO;
import backend.biddingwars.dto.AuctionItemDetailDTO;
import backend.biddingwars.dto.AuctionItemRequestDTO;
import backend.biddingwars.exception.InvalidOperationException;
import backend.biddingwars.exception.ResourceNotFoundException;
import backend.biddingwars.exception.UnauthorizedException;
import backend.biddingwars.exception.ValidationException;
import backend.biddingwars.mapper.AuctionItemMapper;
import backend.biddingwars.model.AuctionItem;
import backend.biddingwars.model.Bid;
import backend.biddingwars.model.Category;
import backend.biddingwars.model.Purchase;
import backend.biddingwars.model.PurchaseStatus;
import backend.biddingwars.model.Status;
import backend.biddingwars.model.User;
import backend.biddingwars.repository.AuctionItemRepository;
import backend.biddingwars.repository.BidRepository;
import backend.biddingwars.repository.CategoryRepository;
import backend.biddingwars.repository.PurchaseRepository;
import jakarta.persistence.OptimisticLockException;

/**
 * Service class for auction item operations.
 * Handles business logic for creating, updating, retrieving, and managing auctions.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 29-01-2026
 */
@Service
@Transactional
public class AuctionItemService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionItemService.class);

    private final AuctionItemRepository auctionItemRepository;
    private final CategoryRepository categoryRepository;
    private final BidRepository bidRepository;
    private final PurchaseRepository purchaseRepository;
    private final AuctionItemMapper auctionItemMapper;
    
    // Payment deadline in hours (48 hours = 2 days)
    private static final int PAYMENT_DEADLINE_HOURS = 48;

    public AuctionItemService(AuctionItemRepository auctionItemRepository,
                              CategoryRepository categoryRepository,
                              BidRepository bidRepository,
                              PurchaseRepository purchaseRepository,
                              AuctionItemMapper auctionItemMapper) {
        this.auctionItemRepository = auctionItemRepository;
        this.categoryRepository = categoryRepository;
        this.bidRepository = bidRepository;
        this.purchaseRepository = purchaseRepository;
        this.auctionItemMapper = auctionItemMapper;
    }


    /**
     * Creates a new auction item.
     *
     * @param requestDTO the auction item data
     * @param owner the user creating the auction
     * @return the created auction as a detail DTO
     * @throws ValidationException if end time is before start time or categories are invalid
     */
    public AuctionItemDetailDTO createAuction(AuctionItemRequestDTO requestDTO, User owner) {
        logger.info("Creating new auction '{}' for user {}", requestDTO.title(), owner.getUsername());

        // Validate time constraints
        validateAuctionTimes(requestDTO.startTime(), requestDTO.endTime());

        // Convert DTO to entity
        AuctionItem auctionItem = auctionItemMapper.toEntity(requestDTO);
        auctionItem.setOwner(owner);

        // Set initial status based on start time
        if (requestDTO.startTime().isAfter(LocalDateTime.now())) {
            auctionItem.setStatus(Status.PENDING);
        } else {
            auctionItem.setStatus(Status.ACTIVE);
        }

        // Fetch and set categories
        if (!requestDTO.categoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(requestDTO.categoryIds());
            if (categories.size() != requestDTO.categoryIds().size()) {
                throw new ValidationException("One or more category IDs are invalid");
            }
            auctionItem.setCategories(categories);
        }

        AuctionItem savedItem = auctionItemRepository.save(auctionItem);
        logger.info("Auction created with ID: {}", savedItem.getId());

        return auctionItemMapper.toDetailDTO(savedItem);
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Gets all active auctions for listing (with thumbnails).
     * Active auctions have status ACTIVE and end time in the future.
     *
     * @return list of auction summary DTOs
     */
    @Transactional(readOnly = true)
    public List<AuctionItemDTO> getActiveAuctions() {
        List<AuctionItem> activeAuctions = auctionItemRepository.findActiveAuctions(LocalDateTime.now());
        return auctionItemMapper.toDTOList(activeAuctions);
    }

    /**
     * Gets all active auctions with pagination.
     *
     * @param pageable pagination parameters
     * @return page of auction summary DTOs
     */
    @Transactional(readOnly = true)
    public Page<AuctionItemDTO> getActiveAuctions(Pageable pageable) {
        Page<AuctionItem> auctionPage = auctionItemRepository.findActiveAuctions(LocalDateTime.now(), pageable);
        return auctionPage.map(auctionItemMapper::toDTO);
    }

    /**
     * Gets a single auction by ID with full details.
     *
     * @param id the auction ID
     * @return the auction detail DTO
     * @throws EntityNotFoundException if auction not found
     */
    @Transactional(readOnly = true)
    public AuctionItemDetailDTO getAuctionById(Long id) {
        AuctionItem auctionItem = findAuctionOrThrow(id);
        return auctionItemMapper.toDetailDTO(auctionItem);
    }

    /**
     * Gets all auctions owned by a specific user.
     *
     * @param userId the owner's user ID
     * @return list of auction summary DTOs
     */
    @Transactional(readOnly = true)
    public List<AuctionItemDTO> getAuctionsByOwner(Long userId) {
        List<AuctionItem> auctions = auctionItemRepository.findByOwnerId(userId);
        return auctionItemMapper.toDTOList(auctions);
    }

    /**
     * Gets auctions by category.
     *
     * @param categoryId the category ID
     * @return list of auction summary DTOs
     */
    @Transactional(readOnly = true)
    public List<AuctionItemDTO> getAuctionsByCategory(Long categoryId) {
        List<AuctionItem> auctions = auctionItemRepository.findByCategoryId(categoryId);
        return auctionItemMapper.toDTOList(auctions);
    }

    /**
     * Searches auctions by title or description.
     *
     * @param searchTerm the search term
     * @param pageable pagination parameters
     * @return page of matching auction DTOs
     */
    @Transactional(readOnly = true)
    public Page<AuctionItemDTO> searchAuctions(String searchTerm, Pageable pageable) {
        Page<AuctionItem> results = auctionItemRepository.searchByTitleOrDescription(searchTerm, pageable);
        return results.map(auctionItemMapper::toDTO);
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Updates an existing auction.
     * Only the owner can update their auction.
     * Cannot update if auction has bids or has ended.
     *
     * @param id the auction ID
     * @param requestDTO the updated auction data
     * @param currentUser the user making the request
     * @return the updated auction detail DTO
     * @throws ResourceNotFoundException if auction not found
     * @throws InvalidOperationException if auction cannot be updated
     * @throws UnauthorizedException if user is not the owner
     */
    public AuctionItemDetailDTO updateAuction(Long id, AuctionItemRequestDTO requestDTO, User currentUser) {
        AuctionItem auctionItem = findAuctionOrThrow(id);

        // Check ownership
        if (!auctionItem.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only update your own auctions");
        }

        // Check if auction can be updated
        if (auctionItem.getBids() != null && !auctionItem.getBids().isEmpty()) {
            throw new InvalidOperationException("Cannot update auction that has bids");
        }

        if (auctionItem.getAuctionEndTime().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("Cannot update ended auction");
        }

        // Validate time constraints
        validateAuctionTimes(requestDTO.startTime(), requestDTO.endTime());

        // Update fields
        auctionItemMapper.updateEntityFromDTO(auctionItem, requestDTO);

        // Update categories if provided
        if (!requestDTO.categoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(requestDTO.categoryIds());
            auctionItem.setCategories(categories);
        }

        try {
            AuctionItem savedItem = auctionItemRepository.save(auctionItem);
            logger.info("Auction {} updated successfully", id);
            return auctionItemMapper.toDetailDTO(savedItem);
        } catch (OptimisticLockException e) {
            logger.warn("Concurrent update detected for auction {}", id);
            throw new InvalidOperationException("Auction was modified by another user. Please refresh and try again.");
        }
    }

    /**
     * Adds images to an auction.
     *
     * @param id the auction ID
     * @param imageUrls list of image URLs to add
     * @param currentUser the user making the request
     * @return the updated auction detail DTO
     */
    public AuctionItemDetailDTO addImages(Long id, List<String> imageUrls, User currentUser) {
        AuctionItem auctionItem = findAuctionOrThrow(id);

        if (!auctionItem.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only add images to your own auctions");
        }

        auctionItem.getImageUrls().addAll(imageUrls);
        AuctionItem savedItem = auctionItemRepository.save(auctionItem);

        return auctionItemMapper.toDetailDTO(savedItem);
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Deletes an auction.
     * Only the owner or an admin can delete an auction.
     * Cannot delete if auction has bids (unless admin).
     *
     * @param id the auction ID
     * @param currentUser the user making the request
     * @param isAdmin whether the current user is an admin
     * @throws ResourceNotFoundException if auction not found
     * @throws InvalidOperationException if auction cannot be deleted
     * @throws UnauthorizedException if user is not authorized
     */
    public void deleteAuction(Long id, User currentUser, boolean isAdmin) {
        AuctionItem auctionItem = findAuctionOrThrow(id);

        boolean isOwner = auctionItem.getOwner().getId().equals(currentUser.getId());

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You can only delete your own auctions");
        }

        // Non-admin owners cannot delete auctions with bids
        if (!isAdmin && auctionItem.getBids() != null && !auctionItem.getBids().isEmpty()) {
            throw new InvalidOperationException("Cannot delete auction that has bids. Contact admin for assistance.");
        }

        auctionItemRepository.delete(auctionItem);
        logger.info("Auction {} deleted by user {}", id, currentUser.getUsername());
    }

    /**
     * Admin method to delete any auction (for inappropriate content).
     *
     * @param id the auction ID
     */
    public void adminDeleteAuction(Long id) {
        AuctionItem auctionItem = findAuctionOrThrow(id);
        auctionItemRepository.delete(auctionItem);
        logger.info("Auction {} deleted by admin", id);
    }

    // ==================== STATUS MANAGEMENT ====================

    /**
     * Activates an auction (changes status from PENDING to ACTIVE).
     * Called when auction start time is reached.
     *
     * @param id the auction ID
     */
    public void activateAuction(Long id) {
        AuctionItem auctionItem = findAuctionOrThrow(id);

        if (auctionItem.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Only pending auctions can be activated");
        }

        auctionItem.setStatus(Status.ACTIVE);
        auctionItemRepository.save(auctionItem);
        logger.info("Auction {} activated", id);
    }

    /**
     * Ends an auction and determines if it was sold.
     * Called when auction end time is reached.
     * Creates a Purchase record if there were bids.
     *
     * @param id the auction ID
     */
    public void endAuction(Long id) {
        AuctionItem auctionItem = findAuctionOrThrow(id);

        if (auctionItem.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Only active auctions can be ended");
        }

        // Determine final status based on whether there were bids
        if (auctionItem.getBids() != null && !auctionItem.getBids().isEmpty()) {
            auctionItem.setStatus(Status.SOLD);
            auctionItemRepository.save(auctionItem);
            
            // Create purchase record for the winner
            createPurchaseForAuction(auctionItem);
            
            logger.info("Auction {} ended - SOLD, purchase created", id);
        } else {
            auctionItem.setStatus(Status.INACTIVE);
            auctionItemRepository.save(auctionItem);
            logger.info("Auction {} ended - NO BIDS", id);
        }
    }
    
    /**
     * Creates a purchase record when an auction ends with bids.
     *
     * @param auctionItem the auction that was sold
     */
    private void createPurchaseForAuction(AuctionItem auctionItem) {
        // Get the winning bid
        Bid winningBid = bidRepository.findHighestBidForItem(auctionItem.getId())
                .orElseThrow(() -> new IllegalStateException("No bids found for sold auction"));
        
        // Check if purchase already exists
        if (purchaseRepository.findByAuctionItemId(auctionItem.getId()).isPresent()) {
            logger.warn("Purchase already exists for auction {}, skipping", auctionItem.getId());
            return;
        }
        
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
        
        purchaseRepository.save(purchase);
        
        logger.info("Purchase created for auction {} - buyer: {}, amount: {}, deadline: {}",
                auctionItem.getId(), winningBid.getBidder().getUsername(),
                winningBid.getAmount(), purchase.getPaymentDeadline());
    }

    /**
     * Processes expired auctions (scheduled task).
     * Finds all active auctions past their end time and ends them.
     */
    public void processExpiredAuctions() {
        List<AuctionItem> expiredAuctions = auctionItemRepository.findExpiredActiveAuctions(LocalDateTime.now());

        for (AuctionItem auction : expiredAuctions) {
            try {
                endAuction(auction.getId());
            } catch (Exception e) {
                logger.error("Error processing expired auction {}: {}", auction.getId(), e.getMessage());
            }
        }   

        if (!expiredAuctions.isEmpty()) {
            logger.info("Processed {} expired auctions", expiredAuctions.size());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Finds an auction by ID or throws ResourceNotFoundException.
     */
    private AuctionItem findAuctionOrThrow(Long id) {
        return auctionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", id));
    }

    /**
     * Validates that auction end time is after start time.
     */
    private void validateAuctionTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new ValidationException("Auction end time must be after start time");
        }
    }
}
