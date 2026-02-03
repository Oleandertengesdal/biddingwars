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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.biddingwars.dto.AuctionItemDTO;
import backend.biddingwars.dto.AuctionItemDetailDTO;
import backend.biddingwars.dto.AuctionItemRequestDTO;
import backend.biddingwars.model.User;
import backend.biddingwars.service.AuctionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for auction item endpoints.
 * Handles CRUD operations for auction items.
 * Provides public and authenticated endpoints.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 02-02-2026
 */
@RestController
@RequestMapping("/auctions")
@Tag(name = "Auctions", description = "Auction item management endpoints")
public class AuctionController {

    private static final Logger logger = LoggerFactory.getLogger(AuctionController.class);

    private final AuctionItemService auctionItemService;

    public AuctionController(AuctionItemService auctionItemService) {
        this.auctionItemService = auctionItemService;
    }

    // ==================== PUBLIC ENDPOINTS ====================

    /**
     * Get all active auctions with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @param sort sort field
     * @return page of active auctions
     */
    @GetMapping
    @Operation(summary = "Get active auctions", description = "Returns paginated list of active auctions")
    public ResponseEntity<Page<AuctionItemDTO>> getActiveAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "auctionEndTime") String sort) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<AuctionItemDTO> auctions = auctionItemService.getActiveAuctions(pageable);

        logger.info("Fetched page {} of active auctions, size {}, sorted by {}.", page, size, sort);
        
        return ResponseEntity.ok(auctions);
    }

    /**
     * Get a single auction by ID.
     *
     * @param id the auction ID
     * @return the auction details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get auction by ID", description = "Returns detailed auction information")
    public ResponseEntity<AuctionItemDetailDTO> getAuctionById(
            @Parameter(description = "Auction ID") @PathVariable Long id) {
        
        AuctionItemDetailDTO auction = auctionItemService.getAuctionById(id);

        logger.info("Fetched auction with id {}.", id);

        return ResponseEntity.ok(auction);
    }

    /**
     * Search auctions by title or description.
     *
     * @param q search query
     * @param page page number
     * @param size page size
     * @return matching auctions
     */
    @GetMapping("/search")
    @Operation(summary = "Search auctions", description = "Search auctions by title or description")
    public ResponseEntity<Page<AuctionItemDTO>> searchAuctions(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionItemDTO> results = auctionItemService.searchAuctions(q, pageable);

        logger.info("Search query: '{}', Results found: {}", q, results.getTotalElements());
        
        return ResponseEntity.ok(results);
    }

    /**
     * Get auctions by category.
     *
     * @param categoryId the category ID
     * @return auctions in the category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get auctions by category", description = "Returns auctions in a specific category")
    public ResponseEntity<List<AuctionItemDTO>> getAuctionsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        
        logger.info("Fetching auctions for category ID: {}", categoryId);

        List<AuctionItemDTO> auctions = auctionItemService.getAuctionsByCategory(categoryId);
        return ResponseEntity.ok(auctions);
    }

    // ==================== AUTHENTICATED ENDPOINTS ====================

    /**
     * Create a new auction.
     *
     * @param requestDTO the auction data
     * @param currentUser the authenticated user
     * @return the created auction
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create auction", description = "Creates a new auction item")
    public ResponseEntity<AuctionItemDetailDTO> createAuction(
            @Valid @RequestBody AuctionItemRequestDTO requestDTO,
            @AuthenticationPrincipal User currentUser) {
        
        logger.info("User {} creating new auction: {}", currentUser.getUsername(), requestDTO.title());
        
        AuctionItemDetailDTO createdAuction = auctionItemService.createAuction(requestDTO, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuction);
    }

    /**
     * Update an existing auction.
     *
     * @param id the auction ID
     * @param requestDTO the updated auction data
     * @param currentUser the authenticated user
     * @return the updated auction
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update auction", description = "Updates an existing auction (owner only)")
    public ResponseEntity<AuctionItemDetailDTO> updateAuction(
            @PathVariable Long id,
            @Valid @RequestBody AuctionItemRequestDTO requestDTO,
            @AuthenticationPrincipal User currentUser) {
        
        AuctionItemDetailDTO updatedAuction = auctionItemService.updateAuction(id, requestDTO, currentUser);

        logger.info("Updated auction with id {} and user {}.", id, currentUser.getUsername());

        return ResponseEntity.ok(updatedAuction);
    }

    /**
     * Delete an auction.
     *
     * @param id the auction ID
     * @param currentUser the authenticated user
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete auction", description = "Deletes an auction (owner or admin only)")
    public ResponseEntity<Void> deleteAuction(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        auctionItemService.deleteAuction(id, currentUser, isAdmin);

        logger.info("Deleted auction with id {} by user {}.", id, currentUser.getUsername());

        return ResponseEntity.noContent().build();
    }

    /**
     * Get current user's auctions.
     *
     * @param currentUser the authenticated user
     * @return list of user's auctions
     */
    @GetMapping("/my-auctions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my auctions", description = "Returns current user's auctions")
    public ResponseEntity<List<AuctionItemDTO>> getMyAuctions(
            @AuthenticationPrincipal User currentUser) {
        
        List<AuctionItemDTO> auctions = auctionItemService.getAuctionsByOwner(currentUser.getId());

        logger.info("Fetched {} auctions for user {}.", auctions.size(), currentUser.getUsername());

        return ResponseEntity.ok(auctions);
    }
}
