package backend.biddingwars.service;

import backend.biddingwars.dto.AuctionItemDTO;
import backend.biddingwars.dto.AuctionItemDetailDTO;
import backend.biddingwars.dto.AuctionItemRequestDTO;
import backend.biddingwars.mapper.AuctionItemMapper;
import backend.biddingwars.model.AuctionItem;
import backend.biddingwars.model.Category;
import backend.biddingwars.model.Status;
import backend.biddingwars.model.User;
import backend.biddingwars.repository.AuctionItemRepository;
import backend.biddingwars.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    private final AuctionItemMapper auctionItemMapper;

    public AuctionItemService(AuctionItemRepository auctionItemRepository,
                              CategoryRepository categoryRepository,
                              AuctionItemMapper auctionItemMapper) {
        this.auctionItemRepository = auctionItemRepository;
        this.categoryRepository = categoryRepository;
        this.auctionItemMapper = auctionItemMapper;
    }


    /**
     * Creates a new auction item.
     *
     * @param requestDTO the auction item data
     * @param owner the user creating the auction
     * @return the created auction as a detail DTO
     * @throws IllegalArgumentException if end time is before start time
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
        if (requestDTO.categoryIds() != null && !requestDTO.categoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(requestDTO.categoryIds());
            if (categories.size() != requestDTO.categoryIds().size()) {
                throw new IllegalArgumentException("One or more category IDs are invalid");
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
     * @throws EntityNotFoundException if auction not found
     * @throws IllegalStateException if auction cannot be updated
     * @throws SecurityException if user is not the owner
     */
    public AuctionItemDetailDTO updateAuction(Long id, AuctionItemRequestDTO requestDTO, User currentUser) {
        AuctionItem auctionItem = findAuctionOrThrow(id);

        // Check ownership
        if (!auctionItem.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("You can only update your own auctions");
        }

        // Check if auction can be updated
        if (auctionItem.getBids() != null && !auctionItem.getBids().isEmpty()) {
            throw new IllegalStateException("Cannot update auction that has bids");
        }

        if (auctionItem.getAuctionEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot update ended auction");
        }

        // Validate time constraints
        validateAuctionTimes(requestDTO.startTime(), requestDTO.endTime());

        // Update fields
        auctionItemMapper.updateEntityFromDTO(auctionItem, requestDTO);

        // Update categories if provided
        if (requestDTO.categoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(requestDTO.categoryIds());
            auctionItem.setCategories(categories);
        }

        try {
            AuctionItem savedItem = auctionItemRepository.save(auctionItem);
            logger.info("Auction {} updated successfully", id);
            return auctionItemMapper.toDetailDTO(savedItem);
        } catch (OptimisticLockException e) {
            logger.warn("Concurrent update detected for auction {}", id);
            throw new IllegalStateException("Auction was modified by another user. Please refresh and try again.");
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
            throw new SecurityException("You can only add images to your own auctions");
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
     * @throws EntityNotFoundException if auction not found
     * @throws IllegalStateException if auction cannot be deleted
     * @throws SecurityException if user is not authorized
     */
    public void deleteAuction(Long id, User currentUser, boolean isAdmin) {
        AuctionItem auctionItem = findAuctionOrThrow(id);

        boolean isOwner = auctionItem.getOwner().getId().equals(currentUser.getId());

        if (!isOwner && !isAdmin) {
            throw new SecurityException("You can only delete your own auctions");
        }

        // Non-admin owners cannot delete auctions with bids
        if (!isAdmin && auctionItem.getBids() != null && !auctionItem.getBids().isEmpty()) {
            throw new IllegalStateException("Cannot delete auction that has bids. Contact admin for assistance.");
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
            logger.info("Auction {} ended - SOLD", id);
        } else {
            auctionItem.setStatus(Status.INACTIVE);
            logger.info("Auction {} ended - NO BIDS", id);
        }

        auctionItemRepository.save(auctionItem);
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
     * Finds an auction by ID or throws EntityNotFoundException.
     */
    private AuctionItem findAuctionOrThrow(Long id) {
        return auctionItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with ID: " + id));
    }

    /**
     * Validates that auction end time is after start time.
     */
    private void validateAuctionTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new IllegalArgumentException("Auction end time must be after start time");
        }
    }
}
