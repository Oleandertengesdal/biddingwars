package backend.biddingwars.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Abstract base entity class providing common audit fields and optimistic locking.
 * All entities should extend this class to inherit:
 * - Automatic creation timestamp
 * - Automatic last modified timestamp
 * - Version field for optimistic locking (concurrency control)
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class BaseEntity {

    /**
     * Timestamp when the entity was created.
     * Automatically set by JPA Auditing on first persist.
     * Cannot be updated after creation.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the entity was last modified.
     * Automatically updated by JPA Auditing on every update.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Version field for optimistic locking.
     * JPA automatically increments this on each update.
     * If two transactions try to update the same entity simultaneously,
     * one will fail with OptimisticLockException.
     */
    @Version
    @Column(name = "version")
    private Long version;
}
