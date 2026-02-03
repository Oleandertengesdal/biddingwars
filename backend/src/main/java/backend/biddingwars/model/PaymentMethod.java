package backend.biddingwars.model;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "payment_methods")
public class PaymentMethod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType type;

    // For CARD payments
    @Column(name = "card_number_hash", nullable = false)
    private String cardNumberHash;

    @Column(name = "card_last_four", nullable = false)
    private String cardLastFour;

    @Column(name = "card_type", nullable = false)
    private String cardType; // VISA, MASTERCARD, AMEX

    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    // For VIPPS payments
    @Column(name = "vipps_phone_number", nullable = false)
    private String vippsPhoneNumber; // Store masked: *** ** 123

    @Column(name = "vipps_phone_full", nullable = false)
    private String vippsPhoneFull; // Store full for verification: +4712345678

    // Common fields
    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private boolean isDefault = false;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode; // Temporary code for mock verification

    @Column(name = "verification_expiry", nullable = false)
    private LocalDateTime verificationExpiry;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    public String getDisplayName() {
        return switch (type) {
            case CREDIT_CARD, DEBIT_CARD -> cardType + " ending in " + cardLastFour;
            case VIPPS -> "Vipps - " + vippsPhoneNumber;
        };
    }

    public boolean isExpired() {
        if (type == PaymentMethodType.CREDIT_CARD || type == PaymentMethodType.DEBIT_CARD) {
            if (expiryMonth == null || expiryYear == null) {
                return false;
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiry = LocalDateTime.of(expiryYear, expiryMonth, 1, 0, 0).plusMonths(1).minusDays(1);
            return expiry.isBefore(now);
        }
        return false;
    }

    public boolean canBeUsedForPayment() {
        return verified && !isExpired();
    }
}
