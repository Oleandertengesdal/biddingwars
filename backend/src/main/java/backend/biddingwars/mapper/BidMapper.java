package backend.biddingwars.mapper;

import org.springframework.stereotype.Component;

import backend.biddingwars.dto.BidDTO;
import backend.biddingwars.model.Bid;

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
        if (bid == null) {
            return null;
        }
        return new BidDTO(
                bid.getId(),
                bid.getAmount(),
                bid.getBidder() != null ? bid.getBidder().getId() : null,
                bid.getBidder() != null ? bid.getBidder().getUsername() : null,
                bid.getAuctionItem() != null ? bid.getAuctionItem().getId() : null,
                bid.getTimestamp()
        );
    }
}