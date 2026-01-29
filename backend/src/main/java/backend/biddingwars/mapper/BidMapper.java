package backend.biddingwars.mapper;

import backend.biddingwars.dto.BidDTO;
import backend.biddingwars.model.Bid;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mapper class for Bid entity.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 29-01-2026
 */
@Component
public class BidMapper {

    public BidDTO toDTO(Bid bid) {
        return new BidDTO(
                bid.getId(),
                bid.getAmount(),
                bid.getBidder().getId(),
                bid.getAuctionItem().getId(),
                bid.getTimestamp()
        );
    }

    public Bid toEntity(BidDTO bidDTO) {
        Bid bid = new Bid();
        bid.setId(bidDTO.id());
        bid.setAmount(bidDTO.amount());
        // Note: Setting bidder and auctionItem should be handled separately
        // TODO: Implement fetching bidder and auctionItem entities based on their IDs
        bid.setTimestamp(bidDTO.timestamp() != null ? bidDTO.timestamp() : LocalDateTime.now());
        return bid;
    }
}