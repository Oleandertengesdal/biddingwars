package backend.biddingwars.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an auction item.
 * Extends BaseEntity to inherit audit fields (createdAt, updatedAt) and optimistic locking (version).
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
@Entity
@Table(name = "items")
@Data
@EqualsAndHashCode(callSuper = true)
public class AuctionItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal startingPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private LocalDateTime auctionStartTime;

    @NotNull
    @Future
    @Column(nullable = false)
    private LocalDateTime auctionEndTime;

    @OneToMany(mappedBy = "auctionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "item_categories",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ElementCollection
    @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    private Double latitude;
    private Double longitude;

    @Version
    private Long version;

    /**
     * Get the thumbnail image URL for the auction item.
     * Replaces with standard image if no images are available.
     *
     * @return the thumbnail image URL
     */
    public String getThumbnail() {
        return imageUrls.isEmpty() ? "default-placeholder.png" : imageUrls.getFirst();
    }
}
