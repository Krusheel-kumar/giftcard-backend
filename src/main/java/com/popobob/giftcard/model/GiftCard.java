package com.popobob.giftcard.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_code", unique = true, nullable = false, length = 50)
    private String publicCode;

    @Column(name = "share_token", unique = true, length = 64)
    private String shareToken;

    @Column(name = "purchase_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchaseAmount;

    @Column(name = "initial_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal initialBalance;

    @Column(name = "current_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "purchaser_name", nullable = false)
    private String purchaserName;

    @Column(name = "purchaser_mobile", nullable = false, length = 20)
    private String purchaserMobile;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_mobile", length = 20)
    private String recipientMobile;

    @Column(name = "personal_message", columnDefinition = "TEXT")
    private String personalMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GiftCardStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Concurrency control for pessimistic locking or optimistic locking if preferred.
    @Version
    private Long version;
}
