package com.popobob.giftcard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customer_rewards")
public class CustomerReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private Long campaignId;
    private Long rewardDefinitionId;

    @Column(name = "reward_status")
    private String status; // LOCKED, ACTIVE, REDEEMED, EXPIRED

    @Column(unique = true)
    private String couponCode;

    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime redeemedAt;
    
    private String redeemedStoreId;
    private String qrSource;

    private String reminderStatus; // e.g. "REMINDED_2_DAYS", "REMINDED_1_DAY"

    @Version
    private Long version; // Optimistic locking

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
