package backend.biddingwars.mapper;

import backend.biddingwars.dto.AuctionItemDTO;
import backend.biddingwars.dto.AuctionItemDetailDTO;
import backend.biddingwars.dto.AuctionItemRequestDTO;
import backend.biddingwars.model.AuctionItem;
import backend.biddingwars.model.Category;
import backend.biddingwars.model.Status;
import backend.biddingwars.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper class for converting between AuctionItem entities and DTOs.
 * Provides methods for:
 * - Entity to list DTO (for auction listings)
 * - Entity to detail DTO (for single auction view)
 * - Request DTO to entity (for creating new auctions)
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
@Component
public class AuctionItemMapper {

    /**
     * Converts an AuctionItem entity to a summary DTO for list views.
     * Used when displaying auction listings with thumbnails.
     *
     * @param auctionItem the entity to convert
     * @return AuctionItemDTO with summary information
     */
    public AuctionItemDTO toDTO(AuctionItem auctionItem) {
        if (auctionItem == null) {
            return null;
        }
        
        return new AuctionItemDTO(
                auctionItem.getId(),
                auctionItem.getTitle(),
                auctionItem.getCurrentPrice() != null 
                        ? auctionItem.getCurrentPrice() 
                        : auctionItem.getStartingPrice(),
                auctionItem.getThumbnail(),
                auctionItem.getAuctionEndTime(),
                auctionItem.getBids() != null ? auctionItem.getBids().size() : 0
        );
    }

    /**
     * Converts a list of AuctionItem entities to a list of summary DTOs.
     *
     * @param auctionItems the list of entities to convert
     * @return list of AuctionItemDTO
     */
    public List<AuctionItemDTO> toDTOList(List<AuctionItem> auctionItems) {
        if (auctionItems == null) {
            return List.of();
        }
        return auctionItems.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Converts an AuctionItem entity to a detailed DTO for single auction view.
     * Includes all auction details, images, categories, and seller information.
     *
     * @param auctionItem the entity to convert
     * @return AuctionItemDetailDTO with full auction details
     */
    public AuctionItemDetailDTO toDetailDTO(AuctionItem auctionItem) {
        if (auctionItem == null) {
            return null;
        }

        // Extract category names
        List<String> categoryNames = auctionItem.getCategories() != null
                ? auctionItem.getCategories().stream()
                    .map(Category::getName)
                    .toList()
                : List.of();

        // Get seller info safely
        User owner = auctionItem.getOwner();
        String sellerUsername = owner != null ? owner.getUsername() : null;
        Long sellerId = owner != null ? owner.getId() : null;

        return new AuctionItemDetailDTO(
                auctionItem.getId(),
                auctionItem.getTitle(),
                auctionItem.getDescription(),
                auctionItem.getStartingPrice(),
                auctionItem.getCurrentPrice() != null 
                        ? auctionItem.getCurrentPrice() 
                        : auctionItem.getStartingPrice(),
                auctionItem.getAuctionStartTime(),
                auctionItem.getAuctionEndTime(),
                sellerUsername,
                sellerId,
                categoryNames,
                auctionItem.getImageUrls() != null 
                        ? auctionItem.getImageUrls() 
                        : List.of(),
                auctionItem.getStatus(),
                auctionItem.getBids() != null ? auctionItem.getBids().size() : 0,
                auctionItem.getLatitude(),
                auctionItem.getLongitude()
        );
    }

    /**
     * Converts a request DTO to an AuctionItem entity for creation.
     * Note: Categories and owner must be set separately in the service layer.
     *
     * @param requestDTO the request DTO containing auction data
     * @return AuctionItem entity (without categories and owner)
     */
    public AuctionItem toEntity(AuctionItemRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }

        AuctionItem auctionItem = new AuctionItem();
        auctionItem.setTitle(requestDTO.title());
        auctionItem.setDescription(requestDTO.description());
        auctionItem.setStartingPrice(requestDTO.startingPrice());
        auctionItem.setCurrentPrice(requestDTO.startingPrice()); // Initial current price = starting price
        auctionItem.setAuctionStartTime(requestDTO.startTime());
        auctionItem.setAuctionEndTime(requestDTO.endTime());
        auctionItem.setLatitude(requestDTO.latitude());
        auctionItem.setLongitude(requestDTO.longitude());
        auctionItem.setStatus(Status.PENDING); // Default status for new auctions
        
        // Note: Categories should be fetched and set in the service layer
        // Note: Owner should be set in the service layer from authenticated user
        
        return auctionItem;
    }

    /**
     * Updates an existing AuctionItem entity with data from a request DTO.
     * Used for updating auction details.
     *
     * @param existingItem the existing entity to update
     * @param requestDTO the request DTO containing updated data
     */
    public void updateEntityFromDTO(AuctionItem existingItem, AuctionItemRequestDTO requestDTO) {
        if (existingItem == null || requestDTO == null) {
            return;
        }

        existingItem.setTitle(requestDTO.title());
        existingItem.setDescription(requestDTO.description());
        existingItem.setStartingPrice(requestDTO.startingPrice());
        existingItem.setAuctionStartTime(requestDTO.startTime());
        existingItem.setAuctionEndTime(requestDTO.endTime());
        existingItem.setLatitude(requestDTO.latitude());
        existingItem.setLongitude(requestDTO.longitude());
        
        // Note: Categories should be handled separately in the service layer
    }
}
