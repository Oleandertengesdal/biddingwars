package backend.biddingwars.model;

import jakarta.persistence.*;
import lombok.Data;


/**
 * Category entity representing item categories in the auction system.
 * Each category has a unique identifier and a name.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
@Entity
@Table(name = "categories")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
