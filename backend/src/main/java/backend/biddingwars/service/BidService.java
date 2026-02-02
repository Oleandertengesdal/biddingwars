package backend.biddingwars.service;

import backend.biddingwars.dto.BidDTO;
import backend.biddingwars.dto.BidRequestDTO;
import backend.biddingwars.mapper.BidMapper;
import backend.biddingwars.model.AuctionItem;
import backend.biddingwars.model.Bid;
import backend.biddingwars.model.Status;
import backend.biddingwars.model.User;
import backend.biddingwars.repository.AuctionItemRepository;
import backend.biddingwars.repository.BidRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for bid operations.
 * Handles business logic for placing and managing bids.
 * Implements the advanced validation requirements from the project specification.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@Service
@Transactional
public class BidService {

    private static final Logger logger = LoggerFactory.getLogger(BidService.class);

    private final BidRepository bidRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final BidMapper bidMapper;

    public BidService(BidRepository bidRepository,
                      AuctionItemRepository auctionItemRepository,
                      BidMapper bidMapper) {
        this.bidRepository = bidRepository;
        this.auctionItemRepository = auctionItemRepository;
        this.bidMapper = bidMapper;
    }

    /**
     * Place a new bid on an auction item.
     * Implements the advanced validation as specified:
     * 1. Is the auction expired (endTime < now)?
     * 2. Is the bid higher than currentPrice?
     * 3. Is the bidder not the owner of the item?
     *
     * @param bidRequest the bid request containing item ID and amount
     * @param bidder the user placing the bid
     * @return the created bid as DTO
     * @throws EntityNotFoundException if auction item not found
     * @throws IllegalStateException if auction is not active or has ended
     * @throws IllegalArgumentException if bid amount is invalid or user is owner
     */
    public BidDTO placeBid(BidRequestDTO bidRequest, User bidder) {
        logger.info("User {} placing bid of {} on auction {}", 
                bidder.getUsername(), bidRequest.amount(), bidRequest.itemId());

        // Find the auction item
        AuctionItem auctionItem = auctionItemRepository.findById(bidRequest.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with ID: " + bidRequest.itemId()));

        // === VALIDATION 1: Check if auction is expired ===
        if (auctionItem.getAuctionEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This auction has ended. Bidding is no longer allowed.");
        }

        // Check if auction hasn't started yet
        if (auctionItem.getAuctionStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("This auction has not started yet.");
        }

        // Check if auction is active
        if (auctionItem.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("This auction is not active. Current status: " + auctionItem.getStatus());
        }

        // === VALIDATION 2: Check if bidder is not the owner ===
        if (auctionItem.getOwner().getId().equals(bidder.getId())) {
            throw new IllegalArgumentException("You cannot bid on your own auction.");
        }

        // === VALIDATION 3: Check if bid is higher than current price ===
        BigDecimal currentPrice = auctionItem.getCurrentPrice() != null 
                ? auctionItem.getCurrentPrice() 
                : auctionItem.getStartingPrice();

        if (bidRequest.amount().compareTo(currentPrice) <= 0) {
            throw new IllegalArgumentException(
                    String.format("Bid amount must be higher than current price of %s", currentPrice));
        }

        // Optional: Check minimum bid increment (good practice)
        BigDecimal minimumIncrement = calculateMinimumIncrement(currentPrice);
        BigDecimal requiredMinimum = currentPrice.add(minimumIncrement);
        
        if (bidRequest.amount().compareTo(requiredMinimum) < 0) {
            throw new IllegalArgumentException(
                    String.format("Minimum bid is %s (current price %s + minimum increment %s)", 
                            requiredMinimum, currentPrice, minimumIncrement));
        }

        // Create and save the bid
        Bid bid = new Bid();
        bid.setAmount(bidRequest.amount());
        bid.setAuctionItem(auctionItem);
        bid.setBidder(bidder);

        Bid savedBid = bidRepository.save(bid);

        // Update the auction's current price
        auctionItem.setCurrentPrice(bidRequest.amount());
        auctionItemRepository.save(auctionItem);

        logger.info("Bid placed successfully: {} on auction {} by user {}", 
                savedBid.getId(), auctionItem.getId(), bidder.getUsername());

        return bidMapper.toDTO(savedBid);
    }

    /**
     * Calculate minimum bid increment based on current price.
     * Higher prices require higher minimum increments.
     *
     * @param currentPrice the current auction price
     * @return the minimum required increment
     */
    private BigDecimal calculateMinimumIncrement(BigDecimal currentPrice) {
        if (currentPrice.compareTo(new BigDecimal("100")) < 0) {
            return new BigDecimal("1.00");
        } else if (currentPrice.compareTo(new BigDecimal("1000")) < 0) {
            return new BigDecimal("5.00");
        } else if (currentPrice.compareTo(new BigDecimal("10000")) < 0) {
            return new BigDecimal("25.00");
        } else {
            return new BigDecimal("100.00");
        }
    }

    /**
     * Get all bids for an auction item.
     *
     * @param itemId the auction item ID
     * @return list of bids sorted by amount (highest first)
     */
    @Transactional(readOnly = true)
    public List<BidDTO> getBidsForAuction(Long itemId) {
        List<Bid> bids = bidRepository.findByAuctionItemIdOrderByAmountDesc(itemId);
        return bids.stream()
                .map(bidMapper::toDTO)
                .toList();
    }

    /**
     * Get all bids for an auction item with pagination.
     *
     * @param itemId the auction item ID
     * @param pageable pagination parameters
     * @return page of bids
     */
    @Transactional(readOnly = true)
    public Page<BidDTO> getBidsForAuction(Long itemId, Pageable pageable) {
        Page<Bid> bids = bidRepository.findByAuctionItemId(itemId, pageable);
        return bids.map(bidMapper::toDTO);
    }

    /**
     * Get all bids placed by a user.
     *
     * @param userId the user ID
     * @return list of user's bids
     */
    @Transactional(readOnly = true)
    public List<BidDTO> getBidsByUser(Long userId) {
        List<Bid> bids = bidRepository.findByBidderId(userId);
        return bids.stream()
                .map(bidMapper::toDTO)
                .toList();
    }

    /**
     * Get the highest bid for an auction.
     *
     * @param itemId the auction item ID
     * @return the highest bid or null if no bids
     */
    @Transactional(readOnly = true)
    public BidDTO getHighestBid(Long itemId) {
        return bidRepository.findHighestBidForItem(itemId)
                .map(bidMapper::toDTO)
                .orElse(null);
    }

    /**
     * Get the winning bidder for a completed auction.
     *
     * @param itemId the auction item ID
     * @return the winning bid DTO or null if no winner
     * @throws IllegalStateException if auction is not yet completed
     */
    @Transactional(readOnly = true)
    public BidDTO getWinningBid(Long itemId) {
        AuctionItem auctionItem = auctionItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with ID: " + itemId));

        if (auctionItem.getStatus() != Status.SOLD) {
            throw new IllegalStateException("Auction has not been completed yet");
        }

        return bidRepository.findHighestBidForItem(itemId)
                .map(bidMapper::toDTO)
                .orElse(null);
    }

    /**
     * Count the number of bids for an auction.
     *
     * @param itemId the auction item ID
     * @return the bid count
     */
    @Transactional(readOnly = true)
    public long getBidCount(Long itemId) {
        return bidRepository.countByAuctionItemId(itemId);
    }
}
