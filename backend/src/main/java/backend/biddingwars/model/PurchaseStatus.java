package backend.biddingwars.model;

/**
 * Enum representing the status of a purchase.
 *
 * @author Oleander Tengesdal
 * @version 1.1
 * @since 05-02-2026
 */
public enum PurchaseStatus {
    PENDING_PAYMENT,    // Awaiting payment from buyer
    PAYMENT_FAILED,     // Payment deadline passed
    COMPLETED,          // Payment received and confirmed
    CANCELLED,          // Purchase was cancelled
    REFUNDED            // Payment was refunded
}
