package com.popobob.giftcard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reward_redemptions")
public class RewardRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private Long customerRewardId;
    private String couponCode;
    private Long campaignId;
    private String storeId;
    
    private LocalDateTime redeemedAt;
    private String transactionReference;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        redeemedAt = LocalDateTime.now();
    }
}
