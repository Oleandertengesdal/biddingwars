package backend.biddingwars.mapper;

import org.springframework.stereotype.Component;

import backend.biddingwars.dto.PurchaseDTO;
import backend.biddingwars.model.Purchase;

/**
 * Mapper for converting between Purchase entities and DTOs.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 12-02-2026
 */
@Component
public class PurchaseMapper {

    /**
     * Convert Purchase entity to DTO.
     *
     * @param purchase the purchase entity
     * @return the purchase DTO
     */
    public PurchaseDTO toDTO(Purchase purchase) {
        if (purchase == null) {
            return null;
        }

        return new PurchaseDTO(
                purchase.getId(),
                purchase.getAuctionItem() != null ? purchase.getAuctionItem().getId() : null,
                purchase.getAuctionItem() != null ? purchase.getAuctionItem().getTitle() : null,
                purchase.getAuctionItem() != null ? purchase.getAuctionItem().getThumbnail() : null,
                purchase.getSeller().getId(),
                purchase.getSeller().getUsername(),
                purchase.getBuyer().getId(),
                purchase.getBuyer().getUsername(),
                purchase.getAmount(),
                purchase.getStatus().name(),
                purchase.getPurchaseDate(),
                purchase.getPaymentDeadline(),
                purchase.getCompletedDate(),
                purchase.isPaymentDefaulted()
        );
    }
}
