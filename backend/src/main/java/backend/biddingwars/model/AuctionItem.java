package backend.biddingwars.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class representing an auction item.
 * Extends BaseEntity to inherit audit fields (createdAt, updatedAt) and optimistic locking (version).
 *
 * @author Oleander Tengesdal
 * @version 2.0
 * @since 02-02-2026
 * @see BaseEntity
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

    @Column
    private Integer antiSnipeMinutes;  // Minutes to add (null = disabled)

    @Column
    private Integer antiSnipeThresholdSeconds = 300;  // Trigger when < X seconds remain

    @Column
    private LocalDateTime originalEndTime;  // Store the original end time

    @Column(nullable = false)
    private int extensionCount = 0;  // Track how many times extended

    private Double latitude;
    private Double longitude;

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
