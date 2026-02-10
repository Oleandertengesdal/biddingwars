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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.biddingwars.dto.BidDTO;
import backend.biddingwars.dto.BidRequestDTO;
import backend.biddingwars.model.User;
import backend.biddingwars.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * REST Controller for bid endpoints.
 * Handles bid placement and retrieval.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@RestController
@RequestMapping("/bids")
@Tag(name = "Bids", description = "Bid management endpoints")
@Validated
public class BidController {

    private static final Logger logger = LoggerFactory.getLogger(BidController.class);

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    /**
     * Place a new bid on an auction.
     * Implements advanced validation:
     * - Checks if auction is still active (not expired)
     * - Checks if bid is higher than current price
     * - Checks that bidder is not the owner
     *
     * @param bidRequest the bid data
     * @param currentUser the authenticated user placing the bid
     * @return the created bid
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Place a bid", description = "Places a new bid on an auction item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bid placed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bid amount or auction ended"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Cannot bid on own auction"),
            @ApiResponse(responseCode = "404", description = "Auction not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent bid conflict")
    })
    public ResponseEntity<BidDTO> placeBid(
            @Valid @RequestBody BidRequestDTO bidRequest,
            @AuthenticationPrincipal User currentUser) {
        
        logger.info("User {} placing bid of {} on auction {}", 
                currentUser.getUsername(), bidRequest.amount(), bidRequest.itemId());
        
        BidDTO createdBid = bidService.placeBid(bidRequest, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBid);
    }

    /**
     * Get all bids for an auction.
     *
     * @param itemId the auction item ID
     * @param page page number
     * @param size page size
     * @return paginated list of bids
     */
    @GetMapping("/auction/{itemId}")
    @Operation(summary = "Get bids for auction", description = "Returns all bids for a specific auction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bids retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Auction not found")
    })
    public ResponseEntity<Page<BidDTO>> getBidsForAuction(
            @Parameter(description = "Auction item ID") @PathVariable Long itemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "amount"));
        Page<BidDTO> bids = bidService.getBidsForAuction(itemId, pageable);

        logger.info("Fetched {} bids for auction {}", bids.getTotalElements(), itemId);
        
        return ResponseEntity.ok(bids);
    }

    /**
     * Get the highest bid for an auction.
     *
     * @param itemId the auction item ID
     * @return the highest bid or 204 if no bids
     */
    @GetMapping("/auction/{itemId}/highest")
    @Operation(summary = "Get highest bid", description = "Returns the highest bid for an auction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Highest bid found"),
            @ApiResponse(responseCode = "204", description = "No bids found"),
            @ApiResponse(responseCode = "404", description = "Auction not found")
    })
    public ResponseEntity<BidDTO> getHighestBid(
            @Parameter(description = "Auction item ID") @PathVariable Long itemId) {
        
        BidDTO highestBid = bidService.getHighestBid(itemId);
        
        if (highestBid == null) {
            logger.info("No bids found for auction {}", itemId);
            return ResponseEntity.noContent().build();
        }
        
        logger.info("Fetched highest bid of {} for auction {}", highestBid.amount(), itemId);

        return ResponseEntity.ok(highestBid);
    }

    /**
     * Get current user's bids.
     *
     * @param currentUser the authenticated user
     * @return list of user's bids
     */
    @GetMapping("/my-bids")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my bids", description = "Returns all bids placed by the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bids retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<BidDTO>> getMyBids(
            @AuthenticationPrincipal User currentUser) {
        
        List<BidDTO> bids = bidService.getBidsByUser(currentUser.getId());
        logger.info("Fetched {} bids for user {}", bids.size(), currentUser.getUsername());
        return ResponseEntity.ok(bids);
    }

    /**
     * Get bid count for an auction.
     *
     * @param itemId the auction item ID
     * @return the number of bids
     */
    @GetMapping("/auction/{itemId}/count")
    @Operation(summary = "Get bid count", description = "Returns the number of bids for an auction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bid count retrieved"),
            @ApiResponse(responseCode = "404", description = "Auction not found")
    })
    public ResponseEntity<Long> getBidCount(
            @Parameter(description = "Auction item ID") @PathVariable Long itemId) {
        
        long count = bidService.getBidCount(itemId);
        logger.info("Fetched bid count {} for auction {}", count, itemId);
        return ResponseEntity.ok(count);
    }
}
